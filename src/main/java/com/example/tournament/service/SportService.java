package com.example.tournament.service;

import com.example.tournament.entity.Sport;
import com.example.tournament.entity.SportRule;
import com.example.tournament.entity.User;
import com.example.tournament.enums.CommonStatus;
import com.example.tournament.enums.TournamentStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.admin.RuleRequest;
import com.example.tournament.payload.request.admin.SportCreateRequest;
import com.example.tournament.payload.request.admin.SportUpdateRequest;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.payload.response.admin.RuleResponse;
import com.example.tournament.payload.response.admin.SportResponse;
import com.example.tournament.repository.SportRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.security.userdetail.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SportService {
    private final SportRepository sportRepository;
    private final TournamentRepository tournamentRepository;

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

    @Transactional
    public SportResponse updateSport(Long id, SportUpdateRequest request) {
        // Tìm môn thể thao hiện tại
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy môn thể thao này"));

        // Kiểm tra trùng tên
        if (!sport.getName().equals(request.getName()) && sportRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên môn thể thao đã tồn tại");
        }

        // Cập nhật thông tin cơ bản
        sport.setName(request.getName());
        sport.setDescription(request.getDescription());

        // Xử lý Smart Merge cho danh sách luật
        updateRules(sport, request.getRules());

        // Lưu
        Sport updatedSport = sportRepository.save(sport);
        return mapToSportResponse(updatedSport);
    }

    private void updateRules(Sport sport, List<RuleRequest> requestedRules) {
        // Tạo Map từ danh sách luật hiện tại trong DB để đối chiếu
        Map<String, SportRule> existingRulesMap = sport.getRules().stream()
                .collect(Collectors.toMap(SportRule::getRuleKey, Function.identity()));

        if (requestedRules == null) return;

        // Danh sách các luật sẽ được giữ lại hoặc thêm mới
        List<SportRule> updatedRulesList = new ArrayList<>();

        for (RuleRequest ruleReq : requestedRules) {
            String key = ruleReq.getRuleKey();

            if (existingRulesMap.containsKey(key)) {
                // TRƯỜNG HỢP 1: Luật đã tồn tại -> Cập nhật giá trị và mô tả
                SportRule existingRule = existingRulesMap.get(key);
                existingRule.setRuleValue(ruleReq.getRuleValue());
                existingRule.setDescription(ruleReq.getDescription());
                updatedRulesList.add(existingRule);

                // Xóa khỏi Map để đánh dấu là luật này "vẫn còn dùng"
                existingRulesMap.remove(key);
            } else {
                // TRƯỜNG HỢP 2: Luật mới hoàn toàn -> Khởi tạo mới
                SportRule newRule = SportRule.builder()
                        .ruleKey(key)
                        .ruleValue(ruleReq.getRuleValue())
                        .description(ruleReq.getDescription())
                        .sport(sport)
                        .build();
                updatedRulesList.add(newRule);
            }
        }

        // TRƯỜNG HỢP 3: Những luật còn sót lại trong Map là những luật bị xóa
        sport.getRules().clear();
        sport.getRules().addAll(updatedRulesList);
    }

    @Transactional
    public SportResponse updateStatus(Long id, CommonStatus newStatus) {
        // Tìm môn thể thao
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy môn thể thao này"));

        // Logic kiểm tra ràng buộc khi KHÓA môn thể thao
        if (newStatus == CommonStatus.INACTIVE) {
            // Danh sách các trạng thái giải đấu không cho phép khóa môn học
            List<TournamentStatus> activeStatuses = Arrays.asList(
                    TournamentStatus.DRAFT,
                    TournamentStatus.REGISTRATION_OPEN,
                    TournamentStatus.ONGOING
            );

            boolean hasActiveTournaments = tournamentRepository.existsBySportIdAndStatusIn(id, activeStatuses);

            if (hasActiveTournaments) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Không thể khóa môn này vì đang có giải đấu trong trạng thái chuẩn bị, đăng ký hoặc đang diễn ra.");
            }
        }

        // Thực hiện cập nhật nếu vượt qua kiểm tra
        sport.setStatus(newStatus);
        Sport updatedSport = sportRepository.save(sport);

        return mapToSportResponse(updatedSport);
    }

}
