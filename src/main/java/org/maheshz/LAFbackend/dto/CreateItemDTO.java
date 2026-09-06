package org.maheshz.LAFbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.maheshz.LAFbackend.enums.ItemCategory;
import org.maheshz.LAFbackend.enums.ItemType;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class CreateItemDTO {
    @NotBlank
    private String title;
    @NotNull
    private ItemCategory category;
    @NotNull
    private ItemType type;
    @NotBlank
    private String description;

    private List<String> imageUrls;
    private Double latitude;
    private Double longitude;
    @NotBlank
    private String addressText;
    private String secretVerificationQuestion;
    private String secretAnswer;

    @NotNull
    private OffsetDateTime dateLostOrFound;
}
