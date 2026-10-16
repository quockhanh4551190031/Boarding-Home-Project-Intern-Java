package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.PublicHouseItem;
import tqkhanh.project.boardinghomeproject.dto.PublicProfileResponse;
import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.BoardingHouseRepository;
import tqkhanh.project.boardinghomeproject.repository.ForumPostRepository;
import tqkhanh.project.boardinghomeproject.repository.UserRepository;
import tqkhanh.project.boardinghomeproject.util.DisplayNames;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PublicProfileService {

    private final UserRepository userRepository;
    private final BoardingHouseRepository houseRepository;
    private final ForumPostRepository postRepository;

    @Transactional(readOnly = true)
    public PublicProfileResponse get(User viewer, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        boolean viewerIsAdmin = viewer != null && viewer.getRole() == Role.ADMIN;
        if ("BLOCKED".equals(user.getStatus()) && !viewerIsAdmin) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng");
        }

        boolean self = viewer != null && viewer.getId().equals(user.getId());
        boolean landlord = user.getRole() == Role.LANDLORD;
        Profile profile = user.getProfile();

        // Chỉ chủ trọ (và chính chủ) mới có SĐT trên hồ sơ công khai
        String phone = null;
        boolean masked = false;
        String raw = profile != null ? profile.getPhone() : null;
        if (raw != null && !raw.isBlank() && (landlord || self)) {
            if (viewer == null) {
                // Muốn hiện SĐT công khai cho cả khách chưa đăng nhập: đổi 2 dòng dưới thành phone = raw;
                phone = DisplayNames.maskPhone(raw);
                masked = true;
            } else {
                phone = raw;
            }
        }

        List<PublicHouseItem> houses = List.of();
        if (landlord) {
            houses = houseRepository.findByLandlordId(user.getId()).stream()
                    .filter(h -> h.getStatus() == HouseStatus.ACTIVE)
                    .map(this::toHouseItem)
                    .toList();
        }
        int roomCount = houses.stream().mapToInt(PublicHouseItem::roomCount).sum();
        int availableCount = houses.stream().mapToInt(PublicHouseItem::availableRoomCount).sum();

        long posts = postRepository.countByUserIdAndStatus(user.getId(), PostStatus.ACTIVE);

        return new PublicProfileResponse(
                user.getId(), DisplayNames.of(user), user.getRole().name(),
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getBio() : null,
                user.getCreatedAt(), phone, masked, self,
                posts, roomCount, availableCount, houses
        );
    }

    private PublicHouseItem toHouseItem(BoardingHouse house) {
        List<Room> visible = house.getRooms().stream()
                .filter(r -> r.getStatus() != RoomStatus.HIDDEN)
                .toList();

        int available = (int) visible.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        BigDecimal minPrice = visible.stream().map(Room::getPrice).min(Comparator.naturalOrder()).orElse(null);

        List<RoomImage> images = visible.stream().flatMap(r -> r.getImages().stream()).toList();
        String thumbnail = images.stream().filter(RoomImage::isThumbnail).findFirst()
                .or(() -> images.stream().findFirst())
                .map(RoomImage::getImageUrl)
                .orElse(null);

        String location = Stream.of(house.getWard(), house.getCity())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));

        return new PublicHouseItem(house.getId(), house.getName(), location,
                visible.size(), available, minPrice, thumbnail);
    }
}