package org.maheshz.LAFbackend.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.entity.Feedback;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.repository.FeedbackRepository;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.maheshz.LAFbackend.service.email.EmailNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    @Data
    public static class FeedbackRequestDTO {
        private String type;
        private String message;
    }

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequestDTO dto, Principal principal) {
        Feedback.FeedbackBuilder feedbackBuilder = Feedback.builder()
                .type(dto.getType())
                .message(dto.getMessage());

        String submitterIdentity = "Anonymous Guest";

        // If the user is logged in, attach their profile to the feedback
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                feedbackBuilder.submittedBy(user);
                submitterIdentity = user.getEmail() + " (" + user.getName() + ")";
            }
        }

        Feedback savedFeedback = feedbackRepository.save(feedbackBuilder.build());

        // Trigger Admin Email Alert instantly
        emailNotificationService.sendFeedbackAlert(
                savedFeedback.getType(),
                savedFeedback.getMessage(),
                submitterIdentity
        );

        return ResponseEntity.ok().body(Map.of("message", "Feedback submitted successfully"));
    }
}