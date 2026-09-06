package org.maheshz.LAFbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimer_id", nullable = false)
    private User claimer;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String proofDescription;

    private String contactEmailOrPhone;

    // --- CONSENT TRACKING DATA ---
    @Column(name = "claimer_latitude")
    private Double claimerLatitude;

    @Column(name = "claimer_longitude")
    private Double claimerLongitude;

    @CreationTimestamp
    private LocalDateTime submittedAt;
}