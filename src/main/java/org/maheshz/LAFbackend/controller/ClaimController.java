package org.maheshz.LAFbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.ClaimRequestDTO;
import org.maheshz.LAFbackend.entity.Chat;
import org.maheshz.LAFbackend.entity.Claim;
import org.maheshz.LAFbackend.entity.Item;
import org.maheshz.LAFbackend.entity.Message;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.exception.ResourceNotFoundException;
import org.maheshz.LAFbackend.repository.ChatRepository;
import org.maheshz.LAFbackend.repository.ClaimRepository;
import org.maheshz.LAFbackend.repository.ItemRepository;
import org.maheshz.LAFbackend.repository.MessageRepository;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final MessageRepository messageRepository;

    @PostMapping
    public ResponseEntity<?> submitClaim(@Valid @RequestBody ClaimRequestDTO dto, Principal principal) {
        User claimer = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // SECURITY FIX (OWASP A01: Broken Access Control): Prevent users from claiming their own items via API
        if (item.getReportedBy().getId().equals(claimer.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Security Exception: You cannot claim an item you reported.\"}");
        }

        // 1. Save the Official Claim Record
        Claim claim = Claim.builder()
                .item(item)
                .claimer(claimer)
                .proofDescription("Secure connection initiated via OTP verification.")
                .contactEmailOrPhone(dto.getContactEmailOrPhone())
                .build();
        claimRepository.save(claim);

        // 2. Automatically Create the Secure Chat Session
        Chat chat = Chat.builder()
                .item(item)
                .status(ItemStatus.OPEN)
                .finder(item.getReportedBy())
                .claimer(claimer)
                .build();
        chat = chatRepository.save(chat);

        // 3. Insert an automated system message
        Message initialMessage = Message.builder()
                .chat(chat)
                .sender(claimer)
                .text("System: User has verified their identity. A secure channel is now open.")
                .isSystemMessage(true)
                .build();
        messageRepository.save(initialMessage);

        chat.setUpdatedAt(initialMessage.getSentAt());
        chatRepository.save(chat);

        return ResponseEntity.ok().body("{\"message\": \"Claim submitted and chat created successfully\"}");
    }
}