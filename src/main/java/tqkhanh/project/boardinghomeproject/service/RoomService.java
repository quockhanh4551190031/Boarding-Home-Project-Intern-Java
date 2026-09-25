package tqkhanh.project.boardinghomeproject.service;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.exception.ForbiddenActionException;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqkhanh.project.boardinghomeproject.specification.RoomSpecifications;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BoardingHouseRepository houseRepository;
    private final AmenityRepository amenityRepository;
    private final RoomImageRepository roomImageRepository;

    @Transactional
    public RoomResponse create(User landlord, RoomRequest request) {
        BoardingHouse house = houseRepository.findByIdAndLandlordId(request.houseId(), landlord.getId())
                .orElseThrow(() -> new ForbiddenActionException(
                        "Bạn không sở hữu nhà trọ này hoặc nhà trọ không tồn tại"));

        Room room = Room.builder()
                .house(house)
                .title(request.title())
                .price(request.price())
                .area(request.area())
                .maxOccupants(request.maxOccupants() != null ? request.maxOccupants() : 1)
                .description(request.description())
                .amenities(resolveAmenities(request.amenityIds()))
                .build();

        roomRepository.save(room);
        return toResponse(room);
    }

    @Transactional
    public RoomResponse update(User landlord, Long roomId, RoomRequest request) {
        Room room = getOwnedRoomOrThrow(landlord.getId(), roomId);

        // Nếu đổi houseId, xác nhận house mới cũng thuộc landlord này
        if (!room.getHouse().getId().equals(request.houseId())) {
            BoardingHouse newHouse = houseRepository.findByIdAndLandlordId(request.houseId(), landlord.getId())
                    .orElseThrow(() -> new ForbiddenActionException("Nhà trọ đích không hợp lệ"));
            room.setHouse(newHouse);
        }

        room.setTitle(request.title());
        room.setPrice(request.price());
        room.setArea(request.area());
        room.setMaxOccupants(request.maxOccupants() != null ? request.maxOccupants() : 1);
        room.setDescription(request.description());
        room.setAmenities(resolveAmenities(request.amenityIds()));

        roomRepository.save(room);
        return toResponse(room);
    }

    @Transactional
    public void delete(User landlord, Long roomId) {
        Room room = getOwnedRoomOrThrow(landlord.getId(), roomId);
        room.setStatus(RoomStatus.HIDDEN);
        roomRepository.save(room);
    }

    @Transactional
    public RoomResponse getById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .filter(r -> r.getStatus() != RoomStatus.HIDDEN)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ"));
        room.setViewCount(room.getViewCount() + 1);
        roomRepository.save(room);
        return toResponse(room);
    }
    @Transactional(readOnly = true)
    public RoomResponse getByIdWithoutIncrementingView(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng trọ"));
        return toResponse(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getByHouse(Long houseId) {
        return roomRepository.findByHouseId(houseId).stream()
                .filter(r -> r.getStatus() != RoomStatus.HIDDEN)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getMyRooms(User landlord) {
        return roomRepository.findAllByLandlordId(landlord.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RoomImageResponse addImage(User landlord, Long roomId, RoomImageRequest request) {
        Room room = getOwnedRoomOrThrow(landlord.getId(), roomId);

        RoomImage image = RoomImage.builder()
                .room(room)
                .imageUrl(request.imageUrl())
                .thumbnail(request.thumbnail())
                .build();

        roomImageRepository.save(image);
        return new RoomImageResponse(image.getId(), image.getImageUrl(), image.isThumbnail());
    }

    @Transactional
    public void deleteImage(User landlord, Long roomId, Long imageId) {
        Room room = getOwnedRoomOrThrow(landlord.getId(), roomId);
        RoomImage image = room.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh này trong phòng"));
        roomImageRepository.delete(image);
    }

    private Room getOwnedRoomOrThrow(Long landlordId, Long roomId) {
        return roomRepository.findByIdAndLandlordId(roomId, landlordId)
                .orElseThrow(() -> new ForbiddenActionException(
                        "Bạn không có quyền thao tác trên phòng này hoặc phòng không tồn tại"));
    }

    private Set<Amenity> resolveAmenities(List<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(amenityRepository.findAllById(amenityIds));
    }

    private RoomResponse toResponse(Room room) {
        return toResponse(room, null);
    }

    private RoomResponse toResponse(Room room, Double distanceKm) {
        List<String> amenityNames = room.getAmenities().stream().map(Amenity::getName).toList();
        List<RoomImageResponse> imageResponses = room.getImages().stream()
                .map(img -> new RoomImageResponse(img.getId(), img.getImageUrl(), img.isThumbnail()))
                .toList();

        return new RoomResponse(
                room.getId(), room.getHouse().getId(), room.getHouse().getName(),
                room.getHouse().getLandlord().getId(),
                room.getHouse().getLatitude().doubleValue(),
                room.getHouse().getLongitude().doubleValue(),
                room.getTitle(), room.getPrice(), room.getArea(), room.getMaxOccupants(),
                room.getDescription(), room.getStatus(), room.getViewCount(),
                amenityNames, imageResponses, room.getCreatedAt(), distanceKm
        );
    }

    @Transactional(readOnly = true)
    public RoomSearchResponse search(
            String keyword,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minArea, BigDecimal maxArea,
            String city,
            List<Long> amenityIds,
            String sortBy,
            int page, int size
    ) {
        Specification<Room> spec = RoomSpecifications.filter(
                keyword, minPrice, maxPrice, minArea, maxArea, city, amenityIds
        );

        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Room> result = roomRepository.findAll(spec, pageable);

        List<RoomResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new RoomSearchResponse(
                content,
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements(),
                result.hasNext()
        );
    }

    private Sort resolveSort(String sortBy){
        if (sortBy == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sortBy.toUpperCase()) {
            case "PRICE_ASC" -> Sort.by(Sort.Direction.ASC, "price");
            case "PRICE_DESC" -> Sort.by(Sort.Direction.DESC, "price"   );
            case "AREA_ASC" -> Sort.by(Sort.Direction.ASC, "area"   );
            case "AREA_DESC" -> Sort.by(Sort.Direction.DESC, "area" );
            case "NEWEST" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    @Transactional(readOnly = true)
    public RoomSearchResponse searchNearby(
            double lat, double lng, double radiusKm,
            BigDecimal minPrice, BigDecimal maxPrice,
            List<Long> amenityIds,
            int page, int size
    ) {
        Specification<Room> spec = RoomSpecifications.filter(
                null, minPrice, maxPrice, null, null, null, amenityIds
        );

        List<Room> matched = roomRepository.findAll(spec);

        record RoomWithDistance(Room room, double distanceKm) {}

        List<RoomWithDistance> ranked = matched.stream()
                .map(r -> new RoomWithDistance(
                        r,
                        haversineKm(lat, lng,
                                r.getHouse().getLatitude().doubleValue(),
                                r.getHouse().getLongitude().doubleValue())
                ))
                .filter(rd -> rd.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(RoomWithDistance::distanceKm))
                .toList();
        int totalElements = ranked.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<RoomResponse> content = ranked.stream()
                .skip((long) page * size)
                .limit(size)
                .map(rd -> toResponse(rd.room(), Math.round(rd.distanceKm()*100) / 100.0))
                .toList();

        return new RoomSearchResponse(content, page, totalPages, totalElements, (page + 1) * size < totalElements);
    }

    // return distance between two coordinates
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;    // earth radius (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)* Math.sin(dLon / 2);
        double c = 2* Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R*c;
    }
}