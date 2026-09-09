package org.maheshz.LAFbackend.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class RealEmailServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;

    private final String ADMIN_EMAIL = "maheshzrawal333@gmail.com";
    private final String BREVO_VERIFIED_SENDER = "maheshzrawal333@gmail.com";

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(BREVO_VERIFIED_SENDER);
            message.setTo(toEmail);
            message.setSubject("Your Secure Verification Code");
            message.setText("Your verification code is: " + otpCode);

            mailSender.send(message);
            log.info("SUCCESS: OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("ERROR: Failed to send OTP email to {}", toEmail, e);
        }
    }

    @Override
    public void sendFeedbackAlert(String type, String message, String submittedBy) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(BREVO_VERIFIED_SENDER);
            mailMessage.setTo(ADMIN_EMAIL);
            mailMessage.setSubject("New Platform Feedback: [" + type + "]");
            mailMessage.setText("FROM: " + submittedBy + "\nMESSAGE: \n" + message);

            mailSender.send(mailMessage);
            log.info("SUCCESS: Feedback email successfully handed off to Brevo!");
        } catch (Exception e) {
            log.error("ERROR: Failed to send feedback email.", e);
        }
    }
}