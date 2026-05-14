package com.example.tournament.config;

import com.example.tournament.entity.Role;
import com.example.tournament.entity.User;
import com.example.tournament.entity.UserRole;
import com.example.tournament.entity.UserRoleId;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.enums.UserStatus;
import com.example.tournament.repository.RoleRepository;
import com.example.tournament.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Bắt đầu kiểm tra và khởi tạo dữ liệu mẫu...");

        // 1. Tạo danh sách các Quyền (Roles)
        initRoles();

        // 2. Tạo các tài khoản
        initAdmin();        // 1 Admin
        initOrganizers();   // 3 BTC
        initClubManagers(); // 10 CLB
        initReferees();     // 2 Trọng tài

        log.info("Hoàn tất khởi tạo dữ liệu mẫu.");
    }

    private void initRoles() {
        for (RoleCode roleCode : RoleCode.values()) {
            if (roleRepository.findByRoleCode(roleCode).isEmpty()) {
                Role role = Role.builder()
                        .roleCode(roleCode)
                        .roleName(roleCode.name())
                        .description("Quyền truy cập " + roleCode.name())
                        .build();
                roleRepository.save(role);
            }
        }
    }

    private void initAdmin() {
        Role adminRole = roleRepository.findByRoleCode(RoleCode.ADMIN).orElseThrow();
        createUserIfNotFound("admin@tournament.com", "System Admin", "my", adminRole);
    }

    private void initOrganizers() {
        Role orgRole = roleRepository.findByRoleCode(RoleCode.ORGANIZER).orElseThrow();
        for (int i = 1; i <= 3; i++) {
            createUserIfNotFound("btc" + i + "@tournament.com", "Ban Tổ Chức " + i, "Btc@123", orgRole);
        }
    }

    private void initClubManagers() {
        Role clubRole = roleRepository.findByRoleCode(RoleCode.CLUB_MANAGER).orElseThrow();
        for (int i = 1; i <= 10; i++) {
            createUserIfNotFound("club" + i + "@tournament.com", "Quản lý CLB " + i, "Club@123", clubRole);
        }
    }

    private void initReferees() {
        Role refRole = roleRepository.findByRoleCode(RoleCode.REFEREE).orElseThrow();
        for (int i = 1; i <= 15; i++) {
            createUserIfNotFound("referee" + i + "@tournament.com", "Trọng tài " + i, "Referee@123", refRole);
        }
    }

    /**
     * Hàm hỗ trợ tạo User và gán Role.
     * Transactional đảm bảo lưu User lấy ID trước, sau đó mới lưu khóa ngoại UserRole.
     */
    private void createUserIfNotFound(String email, String fullName, String password, Role role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = User.builder()
                    .email(email)
                    .fullName(fullName)
                    .passwordHash(passwordEncoder.encode(password))
                    .status(UserStatus.ACTIVE)
                    .build();

            // Lưu lần 1 để Hibernate sinh ra User ID
            user = userRepository.save(user);

            // Tạo khóa phức hợp (EmbeddedId) cho UserRole
            UserRoleId userRoleId = new UserRoleId(user.getId(), role.getId());
            UserRole userRole = UserRole.builder()
                    .id(userRoleId)
                    .user(user)
                    .role(role)
                    .build();

            // Thêm Role vào danh sách của User và lưu lần 2 để cập nhật mapping
            user.getUserRoles().add(userRole);
            userRepository.save(user);

            log.info("Tạo thành công: {} | Mật khẩu: {} | Vai trò: {}", email, password, role.getRoleCode());
        }
    }
}
