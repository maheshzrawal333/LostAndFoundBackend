package org.maheshz.LAFbackend.dto;

import lombok.Builder;
import lombok.Data;
import org.maheshz.LAFbackend.entity.GeoLocation;
import org.maheshz.LAFbackend.enums.ItemCategory;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.enums.ItemType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ItemResponseDTO {
    private UUID id;
    private String title;
    private ItemCategory category;
    private ItemType type;
    private String description;
    private List<String> imageUrls;
    private GeoLocation location;
    private ItemStatus status;
    private boolean hasSecretDetail;
    private UUID reportedByUserId;
    private OffsetDateTime dateLostOrFound;
    private OffsetDateTime createdAt;
    private String secretVerificationQuestion;
    private boolean hasAlreadyClaimed;
}