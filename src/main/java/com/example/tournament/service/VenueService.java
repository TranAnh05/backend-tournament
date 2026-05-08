package com.example.tournament.service;

import com.example.tournament.entity.Court;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.Venue;
import com.example.tournament.enums.CommonStatus;
import com.example.tournament.exception.custom.AppException;
import com.example.tournament.payload.request.admin.VenueCreateRequest;
import com.example.tournament.payload.response.admin.CourtResponse;
import com.example.tournament.payload.response.admin.PageResponse;
import com.example.tournament.payload.response.admin.SportBasicResponse;
import com.example.tournament.payload.response.admin.VenueResponse;
import com.example.tournament.repository.SportRepository;
import com.example.tournament.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;
    private final SportRepository sportRepository;

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
}
