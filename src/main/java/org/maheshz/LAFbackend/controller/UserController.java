package org.maheshz.LAFbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.AuthResponseDTO;
import org.maheshz.LAFbackend.dto.UpdateProfileDTO;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.exception.ResourceNotFoundException;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;

    // Fetch Profile for the App initialization
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO.UserDetailsDTO> getCurrentUser(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(mapToDTO(user));
    }

    // Update Profile from the Account Settings Modal
    @PutMapping("/me")
    public ResponseEntity<AuthResponseDTO.UserDetailsDTO> updateProfile(
            @Valid @RequestBody UpdateProfileDTO dto,
            Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setProvince(dto.getProvince());
        user.setDistrict(dto.getDistrict());

        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(mapToDTO(updatedUser));
    }

    private AuthResponseDTO.UserDetailsDTO mapToDTO(User user) {
        return AuthResponseDTO.UserDetailsDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                // ADD THESE 4 LINES:
                .phone(user.getPhone())
                .province(user.getProvince())
                .district(user.getDistrict())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
