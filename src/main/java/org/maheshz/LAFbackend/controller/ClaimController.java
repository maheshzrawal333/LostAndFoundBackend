package org.maheshz.LAFbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.ClaimRequestDTO;
import org.maheshz.LAFbackend.entity.Chat;
import org.maheshz.LAFbackend.entity.Claim;
import org.maheshz.LAFbackend.entity.Item;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.exception.ResourceNotFoundException;
import org.maheshz.LAFbackend.repository.ChatRepository;
import org.maheshz.LAFbackend.repository.ClaimRepository;
import org.maheshz.LAFbackend.repository.ItemRepository;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.maheshz.LAFbackend.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ClaimController {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final OtpService otpService;

    @PostMapping
    public ResponseEntity<?> submitClaim(@Valid @RequestBody ClaimRequestDTO dto, Principal principal) {
        String userEmail = principal.getName();

        if (!otpService.verifyOtp(userEmail, dto.getOtp())) {
            throw new BadCredentialsException("Invalid or expired verification code.");
        }

        User claimer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (item.getReportedBy().getId().equals(claimer.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Security Exception: You cannot claim an item you reported.\"}");
        }

        if (claimRepository.existsByItemIdAndClaimerId(item.getId(), claimer.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\": \"You have already established a secure connection for this item.\"}");
        }

        Claim claim = Claim.builder()
                .item(item)
                .claimer(claimer)
                .proofDescription("Secure connection initiated via OTP verification.")
                .contactEmailOrPhone(dto.getContactEmailOrPhone())
                .claimerLatitude(dto.getLatitude())
                .claimerLongitude(dto.getLongitude())
                .build();
        claimRepository.save(claim);

        Chat chat = Chat.builder()
                .item(item)
                .status(ItemStatus.OPEN)
                .finder(item.getReportedBy())
                .claimer(claimer)
                .build();

        chatRepository.save(chat);

        return ResponseEntity.ok().body("{\"message\": \"Claim submitted and chat created successfully\"}");
    }
}