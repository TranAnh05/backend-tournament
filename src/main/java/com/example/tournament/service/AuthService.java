package com.example.tournament.service;

import com.example.tournament.entity.Role;
import com.example.tournament.entity.User;
import com.example.tournament.entity.UserRole;
import com.example.tournament.entity.UserRoleId;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.enums.UserStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.auth.LoginRequest;
import com.example.tournament.payload.request.auth.RegisterRequest;
import com.example.tournament.payload.response.auth.AuthResponse;
import com.example.tournament.repository.RoleRepository;
import com.example.tournament.repository.UserRepository;
import com.example.tournament.security.jwt.JwtTokenProvider;
import com.example.tournament.security.userdetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest loginRequest) {
        /*
         * Xác thực người dùng.
         * Nếu sai Email/Mật khẩu -> Ném ra BadCredentialsException.
         * Nếu tài khoản BANNED/INACTIVE -> Ném ra LockedException / DisabledException.
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Lưu thông tin vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo Token
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Lấy thông tin chi tiết của người dùng
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 5. Trả về Response
        return AuthResponse.builder()
                .accessToken(jwt)
                .email(userDetails.getUsername())
                .fullName(userDetails.getUser().getFullName())
                .roles(roles)
                .build();
    }

    @Transactional
    public void register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại hay chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email đã được sử dụng bởi tài khoản khác");
        }

        // Lấy Role mặc định cho người dùng đăng ký mới (Vận động viên)
        Role athleteRole = roleRepository.findByRoleCode(RoleCode.ATHLETE)
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống chưa cấu hình quyền VĐV"));

        // Khởi tạo và lưu User mới
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        // Khởi tạo khóa chính phức hợp và ánh xạ Role cho User
        UserRoleId userRoleId = new UserRoleId(user.getId(), athleteRole.getId());

        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(user)
                .role(athleteRole)
                .build();

        // Thêm Role vào danh sách của User và cập nhật lại
        user.getUserRoles().add(userRole);
        userRepository.save(user);
    }
}
