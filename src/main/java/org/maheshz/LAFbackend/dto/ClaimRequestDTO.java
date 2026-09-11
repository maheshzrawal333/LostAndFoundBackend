package org.maheshz.LAFbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ClaimRequestDTO {
    @NotNull(message = "Item ID is required")
    private UUID itemId;

    @NotBlank
    private String proofDescription;

    private String secretAnswer;

    @NotBlank
    private String contactEmailOrPhone;

    private Double latitude;
    private Double longitude;

    @NotBlank(message = "Verification code is required.")
    private String otp;
}