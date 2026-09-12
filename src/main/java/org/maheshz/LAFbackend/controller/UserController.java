package org.maheshz.LAFbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.AuthResponseDTO;
import org.maheshz.LAFbackend.dto.UpdateProfileDTO;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.exception.ResourceNotFoundException;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.maheshz.LAFbackend.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final OtpService otpService;

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO.UserDetailsDTO> getCurrentUser(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(mapToDTO(user));
    }

    // ALWAYS send OTP to CURRENT email for ANY profile change
    @PostMapping("/me/request-update-otp")
    public ResponseEntity<?> requestProfileUpdateOtp(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        if (isUpdateLimitExceeded(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You can only update your profile 2 times within a 3-month period."));
        }

        otpService.generateAndSendOtp(user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Verification code sent to your current email."));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileDTO dto, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (isUpdateLimitExceeded(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You can only update your profile 2 times within a 3-month period."));
        }

        // Require OTP verification for EVERY change against the CURRENT email
        if (dto.getOtp() == null || !otpService.verifyOtp(user.getEmail(), dto.getOtp())) {
            throw new BadCredentialsException("Invalid verification code.");
        }

        // Apply tracking logic
        applyUpdateTracking(user);

        // Apply changes
        user.setName(dto.getName());
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }
        user.setProvince(dto.getProvince());
        user.setDistrict(dto.getDistrict());

        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(mapToDTO(updatedUser));
    }

    private boolean isUpdateLimitExceeded(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getProfileUpdateCycleStart() == null || user.getProfileUpdateCycleStart().isBefore(now.minusMonths(3))) {
            return false; // Cycle reset
        }
        return user.getProfileUpdateCount() != null && user.getProfileUpdateCount() >= 2;
    }

    private void applyUpdateTracking(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getProfileUpdateCycleStart() == null || user.getProfileUpdateCycleStart().isBefore(now.minusMonths(3))) {
            user.setProfileUpdateCycleStart(now);
            user.setProfileUpdateCount(0);
        }
        user.setProfileUpdateCount((user.getProfileUpdateCount() == null ? 0 : user.getProfileUpdateCount()) + 1);
    }

    private AuthResponseDTO.UserDetailsDTO mapToDTO(User user) {
        return AuthResponseDTO.UserDetailsDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .phone(user.getPhone())
                .province(user.getProvince())
                .district(user.getDistrict())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}