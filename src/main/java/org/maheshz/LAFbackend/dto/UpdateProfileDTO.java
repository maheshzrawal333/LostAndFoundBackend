package org.maheshz.LAFbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 16, message = "Name cannot exceed 16 characters including spaces")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    private String province;
    private String district;
    private String avatarUrl;
    private String otp;
}