package org.maheshz.LAFbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender; // Can be null if it is a system message

    @Column(nullable = false)
    private boolean isSystemMessage;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String attachmentUrl;
    private String attachmentType;
    private String attachmentName;

    @CreationTimestamp
    private LocalDateTime sentAt;
}
