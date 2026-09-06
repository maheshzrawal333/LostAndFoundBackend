package org.maheshz.LAFbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maheshz.LAFbackend.dto.AuthResponseDTO;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.enums.Role;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.maheshz.LAFbackend.security.CustomUserDetailsService;
import org.maheshz.LAFbackend.security.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public boolean doesEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean doesPhoneExist(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    @Transactional
    public AuthResponseDTO standardLogin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[AUTH AUDIT] Failed login attempt for unknown email: {}", email);
                    return new BadCredentialsException("Account not found.");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("[AUTH AUDIT] Failed login attempt (wrong password) for user: {}", user.getId());
            throw new BadCredentialsException("Invalid email or password.");
        }

        log.info("[AUTH AUDIT] Successful login for user: {}", user.getId());
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO registerNewUser(String email, String phone, String password, String otp) {
        if (!otpService.verifyOtp(email, otp)) {
            log.warn("[AUTH AUDIT] Failed registration attempt (invalid OTP) for email: {}", email);
            throw new BadCredentialsException("Invalid verification code.");
        }

        if (password.contains(" ")) {
            throw new BadCredentialsException("Password cannot contain spaces.");
        }

        if (userRepository.findByEmail(email).isPresent() || userRepository.findByPhone(phone).isPresent()) {
            log.warn("[AUTH AUDIT] Failed registration attempt (duplicate details) for email: {}", email);
            throw new BadCredentialsException("An account with these details already exists.");
        }

        User newUser = User.builder()
                .email(email)
                .name(email.split("@")[0])
                .phone(phone != null ? phone : "Not Provided")
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("[AUTH AUDIT] New user registered. User ID: {}", savedUser.getId());
        return generateAuthResponse(savedUser);
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        if (!otpService.verifyOtp(email, otp)) {
            log.warn("[AUTH AUDIT] Password reset failed (invalid OTP) for email: {}", email);
            throw new BadCredentialsException("Invalid verification code.");
        }

        if (newPassword.contains(" ")) {
            throw new BadCredentialsException("Password cannot contain spaces.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Account not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("[AUTH AUDIT] Password reset successful for user: {}", user.getId());
    }

    private AuthResponseDTO generateAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtUtils.generateToken(userDetails);

        AuthResponseDTO.UserDetailsDTO userDto = AuthResponseDTO.UserDetailsDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .province(user.getProvince())
                .district(user.getDistrict())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();

        return AuthResponseDTO.builder().token(jwtToken).user(userDto).build();
    }
}