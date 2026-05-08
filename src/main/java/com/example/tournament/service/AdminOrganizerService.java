package com.example.tournament.service;

import com.example.tournament.entity.User;
import com.example.tournament.payload.response.admin.AdminOrganizerResponse;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrganizerService {
    private final UserRepository userRepository;

    public PageResponse<AdminOrganizerResponse> getOrganizers(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage = userRepository.searchOrganizers(search, status, pageable);

        // Map Entity sang DTO
        List<AdminOrganizerResponse> content = userPage.getContent().stream()
                .map(this::mapToOrganizerResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminOrganizerResponse>builder()
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .content(content)
                .build();
    }

    private AdminOrganizerResponse mapToOrganizerResponse(User user) {
        return AdminOrganizerResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() != null ? user.getStatus().toString() : "ACTIVE")
                .createdAt(user.getCreatedAt())
                .build();
    }
}
