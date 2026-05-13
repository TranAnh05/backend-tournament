package com.example.tournament.service;

import com.example.tournament.entity.Athlete;
import com.example.tournament.entity.Role;
import com.example.tournament.entity.User;
import com.example.tournament.entity.UserRole;
import com.example.tournament.entity.UserRoleId;
import com.example.tournament.enums.RoleCode;
import com.example.tournament.enums.UserStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.athlete.AthleteRegisterRequest;
import com.example.tournament.repository.AthleteRepository;
import com.example.tournament.repository.RoleRepository;
import com.example.tournament.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AthleteRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(AthleteRegisterRequest request) {

        // 1. Kiểm tra email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email đã được sử dụng bởi tài khoản khác");
        }

        // 2. Kiểm tra CCCD trùng
        if (athleteRepository.existsByIdentityNumber(request.getIdentityNumber())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Số CCCD đã được đăng ký bởi tài khoản khác");
        }

        // 3. Lấy Role ATHLETE
        Role athleteRole = roleRepository.findByRoleCode(RoleCode.ATHLETE)
                .orElseThrow(() -> new AppException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống chưa cấu hình quyền VĐV"));

        // 4. Tạo User
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        // 5. Gán Role cho User (trước) — nếu lỗi ở đây thì chưa tạo Athlete
        UserRoleId userRoleId = new UserRoleId(user.getId(), athleteRole.getId());
        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(user)
                .role(athleteRole)
                .build();

        user.getUserRoles().add(userRole);
        userRepository.save(user);

        // 6. Tạo hồ sơ Athlete (sau) — chỉ chạy khi User + Role đã OK
        Athlete athlete = Athlete.builder()
                .user(user)
                .identityNumber(request.getIdentityNumber())
                .dateOfBirth(request.getDateOfBirth())
                .build();

        athleteRepository.save(athlete);
    }

}