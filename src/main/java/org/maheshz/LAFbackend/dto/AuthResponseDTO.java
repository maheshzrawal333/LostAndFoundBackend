package org.maheshz.LAFbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.maheshz.LAFbackend.enums.Role;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private UserDetailsDTO user;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDetailsDTO {
        private UUID id;
        private String email;
        private String name;
        private Role role;
        private String phone;
        private String province;
        private String district;
        private String avatarUrl;
    }
}
