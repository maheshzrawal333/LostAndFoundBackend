package org.maheshz.LAFbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.maheshz.LAFbackend.enums.ItemCategory;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.enums.ItemType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Creates a separate table for multiple image URLs linked to this item
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @Embedded
    private GeoLocation location;

    @Column(nullable = false)
    private OffsetDateTime dateLostOrFound;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.OPEN;

    private boolean hasSecretDetail;
    private String secretVerificationQuestion;
    private String secretAnswerHash; // Hashed answer for security

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private User reportedBy;

}
