package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.PublicHouseItem;
import tqkhanh.project.boardinghomeproject.dto.SearchResponse;
import tqkhanh.project.boardinghomeproject.dto.UserSearchItem;
import tqkhanh.project.boardinghomeproject.entity.HouseStatus;
import tqkhanh.project.boardinghomeproject.entity.Role;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.repository.BoardingHouseRepository;
import tqkhanh.project.boardinghomeproject.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;
    private static final int MAX_LIMIT = 20;

    private final BoardingHouseRepository houseRepository;
    private final ProfileRepository profileRepository;
    private final PublicProfileService publicProfileService;

    @Transactional(readOnly = true)
    public SearchResponse search(User viewer, String rawQuery, int limit) {
        String q = rawQuery == null ? "" : rawQuery.trim().replaceAll("\\s+", " ");
        if (q.length() > MAX_LENGTH) q = q.substring(0, MAX_LENGTH).trim();
        if (q.length() < MIN_LENGTH) return new SearchResponse(List.of(), List.of());

        Pageable page = PageRequest.of(0, Math.min(Math.max(limit, 1), MAX_LIMIT));
        String escaped = escapeLike(q.toLowerCase(Locale.ROOT));
        String pattern = "%" + escaped + "%";
        String prefix = escaped + "%";

        List<PublicHouseItem> houses = houseRepository
                .searchByName(HouseStatus.ACTIVE, pattern, prefix, page).stream()
                .map(publicProfileService::toHouseItem)
                .toList();

        // Khách chưa đăng nhập chỉ tìm được chủ trọ; không công khai danh sách người thuê
        List<Role> roles = viewer == null ? List.of(Role.LANDLORD) : List.of(Role.TENANT, Role.LANDLORD);
        List<UserSearchItem> users = profileRepository.searchByName(roles, pattern, prefix, page).stream()
                .map(p -> new UserSearchItem(
                        p.getUser().getId(), p.getFullName().trim(), p.getUser().getRole().name()))
                .toList();

        return new SearchResponse(houses, users);
    }

    /**
     * Dùng '!' làm ký tự thoát để '%' và '_' do người dùng gõ không bị hiểu là ký tự đại diện.
     */
    private static String escapeLike(String s) {
        return s.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}