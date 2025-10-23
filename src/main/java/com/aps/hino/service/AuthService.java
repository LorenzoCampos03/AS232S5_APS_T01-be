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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    // Token blacklist para invalidar tokens en logout
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

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
            // Verificar si el token está en la lista negra
            if (tokenBlacklist.contains(token)) {
                log.warn("Token is blacklisted (logged out)");
                return Mono.empty();
            }
            
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
    
    /**
     * Logout user by invalidating token
     */
    public Mono<Void> logout(String token) {
        log.debug("Logging out user with token");
        
        try {
            // Agregar token a la lista negra
            tokenBlacklist.add(token);
            
            // Extraer email para logging
            String email = jwtUtil.extractEmail(token);
            log.info("User logged out successfully: {}", email);
            
            return Mono.empty();
        } catch (Exception e) {
            log.error("Error during logout", e);
            return Mono.empty();
        }
    }
    
    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }
}
