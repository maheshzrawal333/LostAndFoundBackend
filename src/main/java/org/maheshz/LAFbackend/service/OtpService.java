package org.maheshz.LAFbackend.service;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.service.email.EmailNotificationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailNotificationService emailNotificationService;
    private static final String MOCK_OTP = "123456"; // Swap with random generation in production

    public void generateAndSendOtp(String email) {
        // In production: generate a 6-digit random string and save it to Redis/DB with an expiration
        emailNotificationService.sendOtpEmail(email, MOCK_OTP);
    }

    public boolean verifyOtp(String email, String otp) {
        // In production: check against Redis/DB
        return MOCK_OTP.equals(otp);
    }
}
