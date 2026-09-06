package org.maheshz.LAFbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maheshz.LAFbackend.security.CustomUserDetailsService;
import org.maheshz.LAFbackend.security.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String userEmail = jwtUtils.extractUsername(token);

                    if (userEmail != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                        if (jwtUtils.isTokenValid(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(authentication);
                            log.info("[WEBSOCKET] Secure STOMP connection established for user: {}", userEmail);
                        }
                    }
                } catch (Exception e) {
                    log.warn("[WEBSOCKET SECURITY] Invalid JWT token attempting to connect to WebSockets");
                    throw new IllegalArgumentException("Invalid Token");
                }
            } else {
                log.warn("[WEBSOCKET SECURITY] Missing or malformed Authorization header");
                throw new IllegalArgumentException("Missing Token");
            }
        }
        return message;
    }
}
