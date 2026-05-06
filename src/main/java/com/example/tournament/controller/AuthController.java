package com.example.tournament.controller;

import com.example.tournament.payload.request.auth.LoginRequest;
import com.example.tournament.payload.request.auth.RegisterRequest;
import com.example.tournament.payload.response.ApiResponse;
import com.example.tournament.payload.response.auth.AuthResponse;
import com.example.tournament.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Đăng nhập thành công")
                .result(authResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpStatus.CREATED.value())
                .message("Đăng ký tài khoản thành công")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
