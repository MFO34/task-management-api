package com.taskmanagement.security;

import com.taskmanagement.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization header'ı al
        final String authHeader = request.getHeader("Authorization");

        // 2. Token var mı kontrol et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // Token yok, devam et
            return;
        }

        // 3. Token'ı ayıkla (Bearer prefix'ini çıkar)
        final String jwt = authHeader.substring(7);  // "Bearer " 7 karakter
        final String userEmail;

        try {
            // 4. Token'dan email çıkar
            userEmail = jwtUtil.extractUsername(jwt);

            // 5. User zaten authenticate olmamışsa devam et
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. User'ı database'den çek
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // 7. Token geçerli mi kontrol et
                if (jwtUtil.validateToken(jwt, userDetails)) {

                    // 8. Authentication object oluştur
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,  // Credentials (password) gerek yok
                            userDetails.getAuthorities()  // Roller (USER, ADMIN)
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. SecurityContext'e kaydet
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token parse hatası, devam et (authentication olmaz)
            System.err.println("JWT Token validation error: " + e.getMessage());
        }

        // 10. Filter chain'e devam et
        filterChain.doFilter(request, response);
    }
}