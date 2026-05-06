package com.example.tournament.security.jwt;

import com.example.tournament.security.userdetail.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Lấy token từ Header của request
            String jwt = getJwtFromRequest(request);

            // 2. Nếu có token và token hợp lệ
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Lấy email từ token
                String email = jwtTokenProvider.getEmailFromJWT(jwt);

                // Lấy thông tin user từ DB thông qua email
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // 3. Nếu user tồn tại, tạo đối tượng Authentication và lưu vào SecurityContext
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Lưu thêm các chi tiết của request (như IP, Session ID)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Xác thực thành công, cấp quyền cho request này đi tiếp
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("Không thể thiết lập xác thực người dùng trong Security Context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
