package org.maheshz.LAFbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maheshz.LAFbackend.dto.ChatResponseDTO;
import org.maheshz.LAFbackend.dto.MessageRequestDTO;
import org.maheshz.LAFbackend.entity.Chat;
import org.maheshz.LAFbackend.entity.Message;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.exception.ResourceNotFoundException;
import org.maheshz.LAFbackend.repository.ChatRepository;
import org.maheshz.LAFbackend.repository.MessageRepository;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<ChatResponseDTO>> getMyChats(Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Chat> chats = chatRepository.findByFinderOrClaimerOrderByUpdatedAtDesc(currentUser, currentUser);

        List<ChatResponseDTO> response = chats.stream().map(chat -> {
            boolean isFinder = chat.getFinder().getId().equals(currentUser.getId());
            User otherUser = isFinder ? chat.getClaimer() : chat.getFinder();

            List<Message> userMessages = chat.getMessages().stream()
                    .filter(msg -> !msg.isSystemMessage())
                    .collect(Collectors.toList());
            String lastMsgText = userMessages.isEmpty() ? "No messages yet" : userMessages.get(userMessages.size() - 1).getText();

            List<ChatResponseDTO.MessageDTO> messageDTOs = chat.getMessages().stream().map(msg -> {
                String senderType = msg.isSystemMessage() ? "system" : msg.getSender().getId().equals(currentUser.getId()) ? "me" : "them";
                ChatResponseDTO.AttachmentDTO attachment = msg.getAttachmentUrl() != null ? ChatResponseDTO.AttachmentDTO.builder().url(msg.getAttachmentUrl()).type(msg.getAttachmentType()).name(msg.getAttachmentName()).build() : null;
                return ChatResponseDTO.MessageDTO.builder().id(msg.getId().toString()).sender(senderType).text(msg.getText()).time(msg.getSentAt()).attachment(attachment).build();
            }).collect(Collectors.toList());

            boolean isPoster = chat.getItem().getReportedBy().getId().equals(currentUser.getId());
            boolean closureRequestedByMe = chat.getClosureRequestedBy() != null && chat.getClosureRequestedBy().getId().equals(currentUser.getId());
            boolean closureRequestedByOther = chat.getClosureRequestedBy() != null && !chat.getClosureRequestedBy().getId().equals(currentUser.getId());

            return ChatResponseDTO.builder()
                    .id(chat.getId())
                    .itemTitle(chat.getItem().getTitle())
                    .itemType(chat.getItem().getType().name()) // --- NEW: Injects LOST or FOUND
                    .reference("REF: TRK-" + chat.getItem().getId().toString().substring(0, 6).toUpperCase())
                    .otherUser(otherUser.getName())
                    .otherUserAvatar(otherUser.getAvatarUrl())
                    .role(isFinder ? "Finder" : "Claimer")
                    .status(chat.getStatus())
                    .lastMessage(lastMsgText)
                    .time(chat.getUpdatedAt())
                    .unread(0)
                    .isPoster(isPoster)
                    .closureRequestedByMe(closureRequestedByMe)
                    .closureRequestedByOther(closureRequestedByOther)
                    .resolutionOtp(isPoster ? chat.getResolutionOtp() : null)
                    .messages(messageDTOs)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable UUID chatId, @RequestBody MessageRequestDTO dto, Principal principal) {
        User sender = userRepository.findByEmail(principal.getName()).orElseThrow();
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        if (chat.getStatus() == ItemStatus.RESOLVED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot send messages to a resolved chat"));
        }

        Message message = Message.builder().chat(chat).sender(sender).text(dto.getText())
                .attachmentUrl(dto.getAttachmentUrl()).attachmentType(dto.getAttachmentType())
                .attachmentName(dto.getAttachmentName()).isSystemMessage(false).build();

        messageRepository.save(message);
        chat.setUpdatedAt(message.getSentAt());
        chatRepository.save(chat);

        broadcastToChat(chatId, "NEW_MESSAGE", Map.of(
                "id", message.getId().toString(),
                "senderId", sender.getId().toString(),
                "text", message.getText(),
                "time", message.getSentAt(),
                "hasAttachment", dto.getAttachmentUrl() != null
        ));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{chatId}/request-resolve-otp")
    public ResponseEntity<?> requestResolveOtp(@PathVariable UUID chatId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        if (!chat.getItem().getReportedBy().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only the original poster can generate the resolution code."));
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        chat.setResolutionOtp(otp);
        chatRepository.save(chat);

        broadcastToChat(chatId, "STATE_UPDATE", Map.of("action", "OTP_GENERATED"));
        return ResponseEntity.ok(Map.of("message", "Code generated securely."));
    }

    @PatchMapping("/{chatId}/cancel-resolve-otp")
    public ResponseEntity<?> cancelResolveOtp(@PathVariable UUID chatId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        if (!chat.getItem().getReportedBy().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only the poster can cancel the code."));
        }

        chat.setResolutionOtp(null);
        chatRepository.save(chat);

        broadcastToChat(chatId, "STATE_UPDATE", Map.of("action", "OTP_CANCELLED"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{chatId}/resolve")
    public ResponseEntity<?> resolveChat(@PathVariable UUID chatId, @RequestBody Map<String, String> payload, Principal principal) {
        String otp = payload.get("otp");
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        if (chat.getResolutionOtp() == null || !chat.getResolutionOtp().equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid verification code."));
        }

        chat.setStatus(ItemStatus.RESOLVED);
        chat.getItem().setStatus(ItemStatus.RESOLVED);
        chatRepository.save(chat);

        broadcastToChat(chatId, "STATE_UPDATE", Map.of("action", "CHAT_RESOLVED"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{chatId}/request-close")
    public ResponseEntity<?> requestCloseChat(@PathVariable UUID chatId, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        chat.setClosureRequestedBy(currentUser);
        chatRepository.save(chat);

        broadcastToChat(chatId, "STATE_UPDATE", Map.of("action", "CLOSURE_REQUESTED", "requesterId", currentUser.getId()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{chatId}/cancel-close")
    public ResponseEntity<?> cancelCloseChat(@PathVariable UUID chatId, Principal principal) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        chat.setClosureRequestedBy(null);
        chatRepository.save(chat);

        broadcastToChat(chatId, "STATE_UPDATE", Map.of("action", "CLOSURE_CANCELLED"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{chatId}/approve-close")
    public ResponseEntity<?> approveCloseChat(@PathVariable UUID chatId, Principal principal) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        chat.setStatus(ItemStatus.RESOLVED);
        chat.setClosureRequestedBy(null);
        chatRepository.save(chat);

        broadcastToChat(chatId, "STATE_UPDATE", Map.of("action", "CHAT_RESOLVED"));
        return ResponseEntity.ok().build();
    }

    private void broadcastToChat(UUID chatId, String type, Map<String, Object> payload) {
        Object wsMessage = Map.of(
                "type", type,
                "data", payload
        );
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, wsMessage);
    }
}