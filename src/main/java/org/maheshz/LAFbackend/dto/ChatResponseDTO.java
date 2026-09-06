package org.maheshz.LAFbackend.dto;

import lombok.Builder;
import lombok.Data;
import org.maheshz.LAFbackend.enums.ItemStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChatResponseDTO {
    private UUID id;
    private String itemTitle;
    private String reference;
    private String otherUser;
    private String otherUserAvatar;
    private String role; // "Finder" or "Claimer"
    private String lastMessage;
    private LocalDateTime time;
    private int unread;
    private ItemStatus status;
    private List<MessageDTO> messages;

    @Data
    @Builder
    public static class MessageDTO {
        private String id;
        private String sender; // "me", "them", or "system"
        private String text;
        private LocalDateTime time;
        private AttachmentDTO attachment;
    }

    @Data
    @Builder
    public static class AttachmentDTO {
        private String type;
        private String url;
        private String name;
    }
}
