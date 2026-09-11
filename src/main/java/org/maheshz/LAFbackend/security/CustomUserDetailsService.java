package org.maheshz.LAFbackend.security;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(emailOrPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailOrPhone));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }
}
