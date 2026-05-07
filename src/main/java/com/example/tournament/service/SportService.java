package com.example.tournament.service;

import com.example.tournament.entity.Sport;
import com.example.tournament.entity.SportRule;
import com.example.tournament.enums.CommonStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.admin.SportCreateRequest;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.payload.response.admin.RuleResponse;
import com.example.tournament.payload.response.admin.SportResponse;
import com.example.tournament.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SportService {
    private final SportRepository sportRepository;

    // ADMIN
    @Transactional
    public SportResponse createSportWithRules(SportCreateRequest request) {
        // Kiểm tra trùng tên môn thể thao
        if (sportRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Môn thể thao này đã tồn tại trên hệ thống");
        }

        // Tao Sport
        Sport sport = Sport.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // Tao luat choi cho sport
        if (request.getRules() != null && !request.getRules().isEmpty()) {
            List<SportRule> sportRules = request.getRules().stream()
                    .map(ruleDto -> SportRule.builder()
                            .ruleKey(ruleDto.getRuleKey())
                            .ruleValue(ruleDto.getRuleValue())
                            .description(ruleDto.getDescription())
                            .sport(sport)
                            .build())
                    .collect(Collectors.toList());

            sport.setRules(sportRules);
        }

        // Lưu vào Database
        Sport savedSport = sportRepository.save(sport);
        return mapToSportResponse(savedSport);
    }

    private SportResponse mapToSportResponse(Sport sport) {
        List<RuleResponse> ruleResponses = sport.getRules().stream()
                .map(rule -> RuleResponse.builder()
                        .id(rule.getId())
                        .ruleKey(rule.getRuleKey())
                        .ruleValue(rule.getRuleValue())
                        .description(rule.getDescription())
                        .build())
                .collect(Collectors.toList());

        return SportResponse.builder()
                .id(sport.getId())
                .name(sport.getName())
                .description(sport.getDescription())
                .status(sport.getStatus().name())
                .rules(ruleResponses)
                .build();
    }

    public PageResponse<SportResponse> getSports(String search, String statusStr, int page, int size) {
        CommonStatus statusEnum = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                statusEnum = CommonStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + statusStr);
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Sport> sportPage = sportRepository.searchSports(search, statusEnum, pageable);

        List<SportResponse> sportResponses = sportPage.getContent().stream()
                .map(this::mapToSportResponse)
                .collect(Collectors.toList());

        return PageResponse.<SportResponse>builder()
                .currentPage(sportPage.getNumber())
                .pageSize(sportPage.getSize())
                .totalPages(sportPage.getTotalPages())
                .totalElements(sportPage.getTotalElements())
                .content(sportResponses)
                .build();
    }
}
