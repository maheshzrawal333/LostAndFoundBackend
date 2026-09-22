package org.maheshz.LAFbackend.controller;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.entity.OtpEntity;
import org.maheshz.LAFbackend.repository.OtpRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Profile("test")
public class TestOnlyController {

    private final OtpRepository otpRepository;

    @GetMapping("/get-otp")
    public ResponseEntity<?> getLatestOtp(@RequestParam String email) {
        OtpEntity otp = otpRepository.findByEmail(email).orElse(null);

        if (otp == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "email", email,
                "otpCode", otp.getOtpCode()
        ));
    }
}