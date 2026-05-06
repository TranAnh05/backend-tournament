package com.example.tournament.security;

import com.example.tournament.security.jwt.JwtAuthenticationEntryPoint;
import com.example.tournament.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Kích hoạt phân quyền trên từng hàm Controller bằng @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Quản lý tiến trình xác thực của Spring Security
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Cấu hình các luồng bảo mật chính (Chặn/Mở API, gắn Filter)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF vì dùng JWT (không dùng Cookie/Session)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Cấu hình CORS để cho phép Frontend (React/Vue/Mobile) gọi API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Xử lý ngoại lệ (Khi chưa đăng nhập)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 4. Thiết lập Session thành Stateless (Không lưu trạng thái trên Server)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. Phân quyền các Endpoint
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả mọi người truy cập API Đăng nhập / Đăng ký
                        .requestMatchers("/auth/**").permitAll()

                        // Phân quyền theo Role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/tournaments/manage/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers("/clubs/manage/**").hasAnyRole("ADMIN", "CLUB_MANAGER")
                        .requestMatchers("/matches/referee/**").hasAnyRole("ADMIN", "ORGANIZER", "REFEREE")
                        .anyRequest().authenticated()
                );

        // 6. Gắn "Người gác cổng" (Jwt Filter) vào trước cổng chính của Spring Security
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));
        configuration.setExposedHeaders(List.of("x-auth-token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
