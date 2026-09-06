package org.maheshz.LAFbackend.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.entity.Feedback;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.repository.FeedbackRepository;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

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

        // If the user is logged in, attach their profile to the feedback
        if (principal != null) {
            userRepository.findByEmail(principal.getName())
                    .ifPresent(feedbackBuilder::submittedBy);
        }

        feedbackRepository.save(feedbackBuilder.build());
        return ResponseEntity.ok().body("Feedback submitted successfully");
    }
}
