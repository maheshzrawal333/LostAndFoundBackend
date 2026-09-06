package org.maheshz.LAFbackend.dto;

import lombok.Data;

@Data
class OtpVerifyDTO {
    private String emailOrPhone;
    private String code;
}
