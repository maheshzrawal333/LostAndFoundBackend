package org.maheshz.LAFbackend.service.email;

public interface EmailNotificationService {
    void sendOtpEmail(String toEmail, String otpCode);
    void sendFeedbackAlert(String type, String message, String submittedBy);
}
