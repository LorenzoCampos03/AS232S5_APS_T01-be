package com.aps.hino.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // We don't need to save context as we're using stateless JWT
        return Mono.empty();
    }
    
    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractUserRole(token);
                    Long userId = jwtUtil.extractUserId(token);
                    
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    );
                    
                    ((UsernamePasswordAuthenticationToken) authentication).setDetails(userId);
                    
                    return Mono.just(new SecurityContextImpl(authentication));
                }
            } catch (Exception e) {
                return Mono.empty();
            }
        }
        
        return Mono.empty();
    }
}
