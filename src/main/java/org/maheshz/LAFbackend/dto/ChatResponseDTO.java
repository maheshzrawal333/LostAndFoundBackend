package org.maheshz.LAFbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.maheshz.LAFbackend.enums.ItemStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDTO {
    private UUID id;
    private String itemTitle;
    private String itemType; // --- NEW: Tells the frontend if it's a LOST or FOUND post
    private String reference;
    private String otherUser;
    private String otherUserAvatar;
    private String role;
    private ItemStatus status;
    private String lastMessage;
    private LocalDateTime time;
    private int unread;

    @JsonProperty("isPoster")
    private boolean isPoster;

    @JsonProperty("closureRequestedByMe")
    private boolean closureRequestedByMe;

    @JsonProperty("closureRequestedByOther")
    private boolean closureRequestedByOther;

    private String resolutionOtp;

    private List<MessageDTO> messages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDTO {
        private String id;
        private String sender;
        private String text;
        private LocalDateTime time;
        private AttachmentDTO attachment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentDTO {
        private String url;
        private String type;
        private String name;
    }
}