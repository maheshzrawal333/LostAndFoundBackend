package org.maheshz.LAFbackend.controller;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.AuthResponseDTO;
import org.maheshz.LAFbackend.dto.OtpRequestDTO;
import org.maheshz.LAFbackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        AuthResponseDTO response = authService.standardLogin(email, password);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody OtpRequestDTO request) {
        // SECURITY CHECK: Do not send OTP if the account already exists
        if (authService.doesEmailExist(request.getEmailOrPhone())) {
            return ResponseEntity.badRequest().body(Map.of("general", "An account with this email already exists. Please sign in instead."));
        }
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("emailOrPhone");
        String code = request.get("code");
        String password = request.get("password");
        String phone = request.get("phone");

        AuthResponseDTO response = authService.registerNewUser(email, phone, password, code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<?> requestPasswordResetOtp(@RequestBody OtpRequestDTO request) {
        // SECURITY CHECK: Do not send OTP if the account doesn't exist
        if (!authService.doesEmailExist(request.getEmailOrPhone())) {
            return ResponseEntity.badRequest().body(Map.of("general", "No account found with this email address."));
        }
        return ResponseEntity.ok(Map.of("message", "Reset code sent successfully."));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        authService.resetPassword(email, code, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset."));
    }
}