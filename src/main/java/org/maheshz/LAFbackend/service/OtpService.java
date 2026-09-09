package org.maheshz.LAFbackend.service;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.service.email.EmailNotificationService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailNotificationService emailNotificationService;

    // Thread-safe in-memory storage for OTPs.
    // (Note: If you scale to multiple backend servers later, you will swap this map for Redis)
    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();

    // Cryptographically strong random number generator
    private final SecureRandom secureRandom = new SecureRandom();

    public void generateAndSendOtp(String email) {
        // Generate a secure 6-digit random number (between 100000 and 999999)
        int randomNum = secureRandom.nextInt(900000) + 100000;
        String otpCode = String.valueOf(randomNum);

        // Store the OTP tied to the email, setting it to expire in 5 minutes
        otpStorage.put(email, new OtpDetails(otpCode, LocalDateTime.now().plusMinutes(5)));

        // Send the real, generated OTP via Brevo
        emailNotificationService.sendOtpEmail(email, otpCode);
    }

    public boolean verifyOtp(String email, String otp) {
        OtpDetails details = otpStorage.get(email);

        // Fail if no OTP was ever requested for this email
        if (details == null) {
            return false;
        }

        // Fail and delete the OTP if 5 minutes have passed
        if (LocalDateTime.now().isAfter(details.expiresAt())) {
            otpStorage.remove(email);
            return false;
        }

        // Success: Delete the OTP so it cannot be reused, then return true
        if (details.code().equals(otp)) {
            otpStorage.remove(email);
            return true;
        }

        // OTP did not match
        return false;
    }

    // Helper record to hold the OTP string and its expiration timestamp
    private record OtpDetails(String code, LocalDateTime expiresAt) {}
}