package org.maheshz.LAFbackend.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DummyEmailServiceImpl implements EmailNotificationService {

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        log.info("\n======================================================\n" +
                "DUMMY EMAIL SERVICE (Ready for Brevo integration)\n" +
                "TO: {}\n" +
                "SUBJECT: Your Secure Verification Code\n" +
                "BODY: Your verification code is: {}\n" +
                "======================================================", toEmail, otpCode);
    }

    @Override
    public void sendFeedbackAlert(String type, String message, String submittedBy) {
        // Updated to your actual developer email address
        log.info("\n======================================================\n" +
                "DUMMY EMAIL SERVICE: ADMIN ALERT\n" +
                "TO: maheshzrawal333@gmail.com\n" +
                "SUBJECT: New Platform Feedback: [{}]\n" +
                "FROM: {}\n" +
                "MESSAGE: \n{}\n" +
                "======================================================", type, submittedBy, message);
    }
}