package com.aps.hino.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.aps.hino.dto.LoginResponse;
import com.aps.hino.dto.UserDto;
import com.aps.hino.model.User;
import com.aps.hino.repository.UserRepository;
import com.aps.hino.security.JwtUtil;
import com.aps.hino.util.PasswordUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Authenticate user with email and password
     */
    public Mono<LoginResponse> authenticate(String email, String password) {
        log.debug("Authenticating user: {}", email);

        return userRepository.findByEmail(email)
                .filter(user -> {
                    // Verificar contraseña con BCrypt
                    boolean matches = PasswordUtil.verifyPassword(password, user.getPasswordHash());
                    if (!matches) {
                        log.warn("Invalid password for user: {}", email);
                    }
                    return matches;
                })
                .map(user -> {
                    String token = generateToken(user);
                    UserDto userDTO = UserDto.fromEntity(user);
                    log.info("User authenticated successfully: {}", email);
                    return new LoginResponse(token, userDTO);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Authentication failed for user: {}", email);
                    return Mono.empty();
                }));
    }

    /**
     * Generate JWT token for user
     */
    public String generateToken(User user) {
        return jwtUtil.generateToken(user);
    }

    /**
     * Validate token and return user
     */
    public Mono<User> validateToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                return userRepository.findByEmail(email);
            }
            return Mono.empty();
        } catch (Exception e) {
            log.error("Error validating token", e);
            return Mono.empty();
        }
    }
}
