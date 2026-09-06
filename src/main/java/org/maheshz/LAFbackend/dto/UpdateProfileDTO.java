package org.maheshz.LAFbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    private String province;
    private String district;
    private String avatarUrl; // Will be set after FileController upload
}
