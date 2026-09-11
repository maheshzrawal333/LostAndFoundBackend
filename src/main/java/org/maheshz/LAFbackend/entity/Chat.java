package org.maheshz.LAFbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finder_id", nullable = false)
    private User finder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimer_id", nullable = false)
    private User claimer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.OPEN;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    @Column(name = "resolution_otp", length = 6)
    private String resolutionOtp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closure_requested_by_id")
    private User closureRequestedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}