package org.maheshz.LAFbackend.service;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.entity.OtpEntity;
import org.maheshz.LAFbackend.repository.OtpRepository;
import org.maheshz.LAFbackend.service.email.EmailNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailNotificationService emailNotificationService;
    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(String email) {
        // Generate a secure 6-digit random number (between 100000 and 999999)
        int randomNum = secureRandom.nextInt(900000) + 100000;
        String otpCode = String.valueOf(randomNum);

        // Fetch existing record, or create a new one if it doesn't exist
        OtpEntity otpEntity = otpRepository.findByEmail(email)
                .orElseGet(() -> OtpEntity.builder().email(email).build());

        // Update the fields with the new code and a fresh 5-minute timer
        otpEntity.setOtpCode(otpCode);
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        // Save handles both INSERT (if new) and UPDATE (if it already existed)
        otpRepository.save(otpEntity);

        // Send the real, generated OTP via Brevo (executes asynchronously)
        emailNotificationService.sendOtpEmail(email, otpCode);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        OtpEntity otpEntity = otpRepository.findByEmail(email).orElse(null);

        // Fail if no OTP exists for this email in the database
        if (otpEntity == null) {
            return false;
        }

        // Check if 5 minutes have passed
        if (LocalDateTime.now().isAfter(otpEntity.getExpiresAt())) {
            otpRepository.delete(otpEntity); // Delete expired code
            return false;
        }

        // Success: If the code matches, delete it so it cannot be reused, and return true
        if (otpEntity.getOtpCode().equals(otp)) {
            otpRepository.delete(otpEntity);
            return true;
        }

        // OTP did not match
        return false;
    }
}