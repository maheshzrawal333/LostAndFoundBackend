package org.maheshz.LAFbackend.controller;

import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ChatResponseDTO>> getMyChats(Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Chat> chats = chatRepository.findByFinderOrClaimerOrderByUpdatedAtDesc(currentUser, currentUser);

        List<ChatResponseDTO> response = chats.stream().map(chat -> {
            boolean isFinder = chat.getFinder().getId().equals(currentUser.getId());
            User otherUser = isFinder ? chat.getClaimer() : chat.getFinder();

            String lastMsgText = chat.getMessages().isEmpty() ? "No messages yet" :
                    chat.getMessages().get(chat.getMessages().size() - 1).getText();

            List<ChatResponseDTO.MessageDTO> messageDTOs = chat.getMessages().stream().map(msg -> {
                String senderType = msg.isSystemMessage() ? "system" :
                        msg.getSender().getId().equals(currentUser.getId()) ? "me" : "them";

                ChatResponseDTO.AttachmentDTO attachment = null;
                if (msg.getAttachmentUrl() != null) {
                    attachment = ChatResponseDTO.AttachmentDTO.builder()
                            .url(msg.getAttachmentUrl())
                            .type(msg.getAttachmentType())
                            .name(msg.getAttachmentName())
                            .build();
                }

                return ChatResponseDTO.MessageDTO.builder()
                        .id(msg.getId().toString())
                        .sender(senderType)
                        .text(msg.getText())
                        .time(msg.getSentAt())
                        .attachment(attachment)
                        .build();
            }).collect(Collectors.toList());

            return ChatResponseDTO.builder()
                    .id(chat.getId())
                    .itemTitle(chat.getItem().getTitle())
                    .reference("REF: TRK-" + chat.getItem().getId().toString().substring(0, 4).toUpperCase())
                    .otherUser(otherUser.getName())
                    .otherUserAvatar(otherUser.getAvatarUrl())
                    .role(isFinder ? "Finder" : "Claimer")
                    .status(chat.getStatus())
                    .lastMessage(lastMsgText)
                    .time(chat.getUpdatedAt())
                    .unread(0) // Logic for unread counts can be added later
                    .messages(messageDTOs)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable UUID chatId, @RequestBody MessageRequestDTO dto, Principal principal) {
        User sender = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        if (chat.getStatus() == ItemStatus.RESOLVED) {
            return ResponseEntity.badRequest().body("Cannot send messages to a resolved chat");
        }

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .text(dto.getText())
                .attachmentUrl(dto.getAttachmentUrl())
                .attachmentType(dto.getAttachmentType())
                .attachmentName(dto.getAttachmentName())
                .isSystemMessage(false)
                .build();

        messageRepository.save(message);
        chat.setUpdatedAt(message.getSentAt());
        chatRepository.save(chat);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{chatId}/resolve")
    public ResponseEntity<?> resolveChat(@PathVariable UUID chatId, Principal principal) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        chat.setStatus(ItemStatus.RESOLVED);
        chat.getItem().setStatus(ItemStatus.RESOLVED);

        Message systemMessage = Message.builder()
                .chat(chat)
                .isSystemMessage(true)
                .text("Item exchange confirmed. Chat resolved and closed to limit platform costs and secure data.")
                .build();

        messageRepository.save(systemMessage);
        chatRepository.save(chat);

        return ResponseEntity.ok().build();
    }
}