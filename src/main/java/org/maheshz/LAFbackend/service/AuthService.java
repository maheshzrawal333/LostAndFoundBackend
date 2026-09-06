package org.maheshz.LAFbackend.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    private final String MOCK_OTP = "123456";

    // --- NEW: Helper method to check database for existing emails ---
    public boolean doesEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public AuthResponseDTO standardLogin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Account not found."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO registerNewUser(String email, String phone, String password, String otp) {
        if (!MOCK_OTP.equals(otp)) {
            throw new BadCredentialsException("Invalid verification code.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadCredentialsException("An account with this email already exists.");
        }

        User newUser = User.builder()
                .email(email)
                .name(email.split("@")[0])
                .phone(phone != null ? phone : "Not Provided")
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        return generateAuthResponse(savedUser);
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        if (!MOCK_OTP.equals(otp)) {
            throw new BadCredentialsException("Invalid verification code.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Account not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .user(userDto)
                .build();
    }
}