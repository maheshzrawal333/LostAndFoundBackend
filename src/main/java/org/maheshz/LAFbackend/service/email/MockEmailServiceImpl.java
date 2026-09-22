package org.maheshz.LAFbackend.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@Profile("test") //This ensures it only runs during Postman automated tests
public class MockEmailServiceImpl implements EmailNotificationService {

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        // Intercepts the email and just prints it to the console
        log.info("[TEST MODE] Mock email successfully 'sent' to: {} | Secure OTP: {}", toEmail, otpCode);
    }

    @Override
    public void sendFeedbackAlert(String type, String message, String submittedBy) {
        // Intercepts the feedback email
        log.info("[TEST MODE] Mock feedback alert 'sent': [{}] from {}", type, submittedBy);
    }
}
