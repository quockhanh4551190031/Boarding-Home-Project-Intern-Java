package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.HouseSearchItem;
import tqkhanh.project.boardinghomeproject.dto.HouseSearchResponse;
import tqkhanh.project.boardinghomeproject.entity.BoardingHouse;
import tqkhanh.project.boardinghomeproject.entity.Room;
import tqkhanh.project.boardinghomeproject.entity.RoomStatus;
import tqkhanh.project.boardinghomeproject.repository.RoomImageRepository;
import tqkhanh.project.boardinghomeproject.repository.RoomRepository;
import tqkhanh.project.boardinghomeproject.specification.RoomSpecifications;
import tqkhanh.project.boardinghomeproject.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HouseSearchService {

    private static final int MAX_SIZE = 100;
    private static final double MAX_RADIUS_KM = 200;

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;

    private static final class Group {
        final BoardingHouse house;
        int rooms;
        BigDecimal minPrice, maxPrice, minArea, maxArea;
        LocalDateTime newest;
        Double distanceKm;

        Group(BoardingHouse house) {
            this.house = house;
        }
    }

    @Transactional(readOnly = true)
    public HouseSearchResponse search(
            String keyword,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minArea, BigDecimal maxArea,
            String city, String ward, List<Long> amenityIds,
            Double lat, Double lng, Double radiusKm,
            String sortBy, int page, int size
    ) {
        validateGeo(lat, lng, radiusKm);
        boolean hasPoint = lat != null && lng != null;

        List<Room> rooms = roomRepository.findAll(RoomSpecifications.filter(
                keyword, minPrice, maxPrice, minArea, maxArea, city, ward, amenityIds));

        Map<Long, Group> grouped = new LinkedHashMap<>();
        for (Room r : rooms) {
            BoardingHouse h = r.getHouse();
            Group g = grouped.computeIfAbsent(h.getId(), id -> new Group(h));
            g.rooms++;
            g.minPrice = lower(g.minPrice, r.getPrice());
            g.maxPrice = higher(g.maxPrice, r.getPrice());
            g.minArea = lower(g.minArea, r.getArea());
            g.maxArea = higher(g.maxArea, r.getArea());
            if (r.getCreatedAt() != null && (g.newest == null || r.getCreatedAt().isAfter(g.newest))) {
                g.newest = r.getCreatedAt();
            }
        }

        List<Group> groups = new ArrayList<>(grouped.values());

        if (hasPoint) {
            for (Group g : groups) {
                g.distanceKm = GeoUtils.haversineKm(lat, lng,
                        g.house.getLatitude().doubleValue(), g.house.getLongitude().doubleValue());
            }
            if (radiusKm != null) {
                groups.removeIf(g -> g.distanceKm > radiusKm);
            }
        }

        groups.sort(comparator(sortBy, hasPoint));

        int total = groups.size();
        long totalRooms = groups.stream().mapToLong(g -> g.rooms).sum();
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int safePage = Math.max(page, 0);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        int from = (int) Math.min((long) safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);

        List<Group> slice = groups.subList(from, to);
        Map<Long, String> covers = loadCovers(slice);
        List<HouseSearchItem> items = slice.stream()
                .map(g -> toItem(g, covers.get(g.house.getId())))
                .toList();

        return new HouseSearchResponse(items, safePage, totalPages, total, totalRooms, to < total);
    }

    private static void validateGeo(Double lat, Double lng, Double radiusKm) {
        if ((lat == null) != (lng == null)) {
            throw new IllegalArgumentException("Cần truyền đủ cả vĩ độ và kinh độ");
        }
        if (lat != null && (!Double.isFinite(lat) || !Double.isFinite(lng)
                || lat < -90 || lat > 90 || lng < -180 || lng > 180)) {
            throw new IllegalArgumentException("Tọa độ không hợp lệ");
        }
        if (radiusKm != null) {
            if (lat == null) throw new IllegalArgumentException("Bán kính chỉ dùng được khi có vị trí");
            if (!Double.isFinite(radiusKm) || radiusKm <= 0 || radiusKm > MAX_RADIUS_KM) {
                throw new IllegalArgumentException("Bán kính phải lớn hơn 0 và không quá 200 km");
            }
        }
    }

    private static Comparator<Group> comparator(String sortBy, boolean hasPoint) {
        String key = sortBy == null ? "" : sortBy.trim().toUpperCase(Locale.ROOT);

        Comparator<Group> newest = Comparator
                .comparing((Group g) -> g.newest, Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder()))
                .reversed();

        Comparator<Group> primary = switch (key) {
            case "DISTANCE" -> hasPoint ? Comparator.comparingDouble((Group g) -> g.distanceKm) : newest;
            case "PRICE_ASC" -> Comparator.comparing((Group g) -> g.minPrice);
            case "PRICE_DESC" -> Comparator.comparing((Group g) -> g.maxPrice).reversed();
            case "AREA_ASC" -> Comparator.comparing((Group g) -> g.minArea);
            case "AREA_DESC" -> Comparator.comparing((Group g) -> g.maxArea).reversed();
            default -> newest;
        };
        // Phụ thêm theo id để thứ tự ổn định giữa các trang
        return primary.thenComparing(g -> g.house.getId());
    }

    private Map<Long, String> loadCovers(List<Group> slice) {
        if (slice.isEmpty()) return Map.of();
        List<Long> ids = slice.stream().map(g -> g.house.getId()).toList();

        Map<Long, String> covers = new HashMap<>();
        for (Object[] row : roomImageRepository.findCoverImages(ids, RoomStatus.HIDDEN)) {
            covers.putIfAbsent(((Number) row[0]).longValue(), (String) row[1]);
        }
        return covers;
    }

    private HouseSearchItem toItem(Group g, String thumbnail) {
        BoardingHouse h = g.house;
        String location = Stream.of(h.getWard(), h.getCity())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
        Double distance = g.distanceKm == null ? null : Math.round(g.distanceKm * 100) / 100.0;

        return new HouseSearchItem(
                h.getId(), h.getName(), location,
                h.getLatitude().doubleValue(), h.getLongitude().doubleValue(),
                g.rooms, g.minPrice, g.maxPrice, thumbnail, distance);
    }

    private static BigDecimal lower(BigDecimal a, BigDecimal b) {
        return a == null || b.compareTo(a) < 0 ? b : a;
    }

    private static BigDecimal higher(BigDecimal a, BigDecimal b) {
        return a == null || b.compareTo(a) > 0 ? b : a;
    }
}