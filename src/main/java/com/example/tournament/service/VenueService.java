package com.example.tournament.service;

import com.example.tournament.entity.Court;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.Venue;
import com.example.tournament.enums.CommonStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.admin.CourtUpdateRequest;
import com.example.tournament.payload.request.admin.VenueCreateRequest;
import com.example.tournament.payload.request.admin.VenueStatusUpdateRequest;
import com.example.tournament.payload.request.admin.VenueUpdateRequest;
import com.example.tournament.payload.response.admin.CourtResponse;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.payload.response.admin.SportBasicResponse;
import com.example.tournament.payload.response.admin.VenueResponse;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.SportRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;
    private final SportRepository sportRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;

    public PageResponse<VenueResponse> getVenues(String search, String statusStr, int page, int size) {
        CommonStatus statusEnum = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                statusEnum = CommonStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + statusStr);
            }
        }

        // Cấu hình phân trang
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Venue> venuePage = venueRepository.searchVenues(search, statusEnum, pageable);

        // Map sang DTO
        List<VenueResponse> venueResponses = venuePage.getContent().stream()
                .map(this::mapToVenueResponse)
                .collect(Collectors.toList());

        return PageResponse.<VenueResponse>builder()
                .currentPage(venuePage.getNumber())
                .pageSize(venuePage.getSize())
                .totalPages(venuePage.getTotalPages())
                .totalElements(venuePage.getTotalElements())
                .content(venueResponses)
                .build();
    }


    private VenueResponse mapToVenueResponse(Venue venue) {
        List<CourtResponse> courtResponses = venue.getCourts().stream()
                // LỌC TRÙNG LẶP THEO ID CỦA SÂN
                .filter(court -> court.getId() != null)
                .collect(Collectors.toMap(
                        Court::getId,
                        court -> court,
                        (existing, replacement) -> existing // Nếu trùng ID, giữ lại cái đầu tiên
                ))
                .values().stream()
                .map(court -> {
                    // Map danh sách môn thể thao hỗ trợ
                    List<SportBasicResponse> sportResponses = court.getSupportedSports().stream()
                            .map(sport -> SportBasicResponse.builder()
                                    .id(sport.getId())
                                    .name(sport.getName())
                                    .build())
                            .collect(Collectors.toList());

                    // Map sân con
                    return CourtResponse.builder()
                            .id(court.getId())
                            .courtName(court.getCourtName())
                            .status(court.getStatus().name())
                            .supportedSports(sportResponses)
                            .build();
                })
                .collect(Collectors.toList());

        // Map địa điểm chính
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .status(venue.getStatus().name())
                .courts(courtResponses)
                .build();
    }

    @Transactional
    public VenueResponse createVenue(VenueCreateRequest request) {
        // Kiem tra trung ten
        if(venueRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên địa điểm thi đấu này đã tồn tại");
        }

        // Tao dia diem thi dau
        Venue venue = Venue.builder()
            .name(request.getName())
            .address(request.getAddress())
            .status(CommonStatus.ACTIVE)
            .build();

        // Tao danh sach con
        if (request.getCourts() != null && !request.getCourts().isEmpty()) {
            Set<Court> courts = request.getCourts().stream()
                    .map(courtReq -> {
                        // Tìm danh sách môn thể thao từ DB dựa trên list IDs
                        List<Sport> sports = sportRepository.findAllById(courtReq.getSupportedSportIds());

                        if (sports.size() != courtReq.getSupportedSportIds().size()) {
                            throw new AppException(HttpStatus.BAD_REQUEST, "Một số môn thể thao được chọn không tồn tại");
                        }

                        // Khởi tạo Court và thiết lập quan hệ
                        return Court.builder()
                                .courtName(courtReq.getCourtName())
                                .status(courtReq.getStatus())
                                .venue(venue)
                                .supportedSports(new HashSet<>(sports)) // N-N với Sport
                                .build();
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            venue.setCourts(courts);
        }

        Venue savedVenue = venueRepository.save(venue);
        return mapToVenueResponse(savedVenue);
    }

    @Transactional
    public VenueResponse updateVenue(Long id, VenueUpdateRequest request) {
        // Tìm và Validate Venue
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy địa điểm"));

        // Kiểm tra trùng tên
        if (!venue.getName().equals(request.getName()) && venueRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Tên địa điểm đã tồn tại");
        }

        // Cập nhật thông tin cơ bản
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());

        // Xử lý Smart Merge danh sách Sân
        updateCourtsSmartly(venue, request.getCourts());

        return mapToVenueResponse(venueRepository.save(venue));
    }

    private void updateCourtsSmartly(Venue venue, List<CourtUpdateRequest> courtRequests) {
        // Lấy danh sách sân gốc
        Set<Court> currentCourts = venue.getCourts();

        // Nhận diện các sân bị Admin xóa
        List<Court> courtsToRemove = new ArrayList<>();
        for (Court existingCourt : currentCourts) {
            boolean isKept = courtRequests.stream()
                    .anyMatch(req -> req.getId() != null && req.getId().equals(existingCourt.getId()));
            if (!isKept) {
                courtsToRemove.add(existingCourt);
            }
        }

        // Xử lý các sân cần xóa
        for (Court courtToRemove : courtsToRemove) {
            if (matchRepository.existsByCourtId(courtToRemove.getId())) {
                // RÀNG BUỘC: Đã có trận đấu -> Soft Delete
                courtToRemove.setDeletedAt(LocalDateTime.now());
            } else {
                currentCourts.remove(courtToRemove);
            }
        }

        // Xử lý Cập nhật sân cũ & Thêm sân mới
        for (CourtUpdateRequest req : courtRequests) {
            if (req.getId() != null) {
                // TRƯỜNG HỢP: Cập nhật sân cũ
                Court existingCourt = currentCourts.stream()
                        .filter(c -> req.getId().equals(c.getId()))
                        .findFirst()
                        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Không tìm thấy sân với ID: " + req.getId()));

                existingCourt.setCourtName(req.getCourtName());
                existingCourt.setStatus(req.getStatus());

                // Cập nhật môn thể thao
                updateSupportedSports(existingCourt, req.getSupportedSportIds());
            } else {
                // TRƯỜNG HỢP: Thêm sân mới
                Court newCourt = Court.builder()
                        .courtName(req.getCourtName())
                        .status(req.getStatus())
                        .venue(venue)
                        .build();

                // Cập nhật môn thể thao cho sân mới
                updateSupportedSports(newCourt, req.getSupportedSportIds());

                // Thêm sân mới vào danh sách hiện tại
                currentCourts.add(newCourt);
            }
        }
    }

    private void updateSupportedSports(Court court, List<Long> sportIds) {
        // Kiểm tra ràng buộc khi GỠ môn thể thao
        if (court.getId() != null) {
            Set<Long> requestedIds = new HashSet<>(sportIds);
            for (Sport existingSport : court.getSupportedSports()) {
                if (!requestedIds.contains(existingSport.getId())) {
                    // Nếu môn này bị gỡ, check xem có trận đấu sắp tới không
                    if (matchRepository.hasUpcomingMatchesForSportOnCourt(court.getId(), existingSport.getId())) {
                        throw new AppException(HttpStatus.BAD_REQUEST,
                                "Không thể gỡ môn " + existingSport.getName() + " khỏi sân " + court.getCourtName() + " vì đang có lịch thi đấu.");
                    }
                }
            }
        }

        List<Sport> sports = sportRepository.findAllById(sportIds);
        court.getSupportedSports().clear();
        court.getSupportedSports().addAll(sports);
    }

    @Transactional
    public VenueResponse updateVenueStatus(Long id, VenueStatusUpdateRequest request) {
        // Tìm địa điểm
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy địa điểm thi đấu"));

        CommonStatus newStatus = request.getStatus();

        // Ràng buộc khi KHÓA
        if (newStatus == CommonStatus.INACTIVE && venue.getStatus() != CommonStatus.INACTIVE) {
            // Ràng buộc: Giải đấu
            if (tournamentRepository.hasActiveTournamentsAtVenue(id)) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Không thể khóa địa điểm vì đang có giải đấu đang diễn ra hoặc sắp tổ chức tại đây.");
            }

            // Ràng buộc: Trận đấu
            if (matchRepository.hasActiveMatchesAtVenue(id)) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Không thể khóa địa điểm vì đang có trận đấu đã lên lịch tại các sân thuộc khu vực này.");
            }
        }

        //  Thực hiện cập nhật
        venue.setStatus(newStatus);

        // Lưu & Trả về
        return mapToVenueResponse(venueRepository.save(venue));
    }
}
