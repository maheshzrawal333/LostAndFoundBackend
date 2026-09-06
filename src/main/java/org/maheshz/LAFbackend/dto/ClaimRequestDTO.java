package org.maheshz.LAFbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ClaimRequestDTO {
    @NotNull
    private UUID itemId;
    @NotBlank
    private String proofDescription;
    private String secretAnswer;
    @NotBlank
    private String contactEmailOrPhone;
}
