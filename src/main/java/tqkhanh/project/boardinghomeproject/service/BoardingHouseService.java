package tqkhanh.project.boardinghomeproject.service;

import tqkhanh.project.boardinghomeproject.dto.BoardingHouseRequest;
import tqkhanh.project.boardinghomeproject.dto.BoardingHouseResponse;
import tqkhanh.project.boardinghomeproject.entity.BoardingHouse;
import tqkhanh.project.boardinghomeproject.entity.HouseStatus;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.exception.ForbiddenActionException;
import tqkhanh.project.boardinghomeproject.exception.ResourceNotFoundException;
import tqkhanh.project.boardinghomeproject.repository.AdministrativeUnitRepository;
import tqkhanh.project.boardinghomeproject.repository.BoardingHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardingHouseService {

    private final BoardingHouseRepository houseRepository;
    private final AdministrativeUnitRepository administrativeUnitRepository;

    @Transactional
    public BoardingHouseResponse create(User landlord, BoardingHouseRequest request) {
        validateLocation(request.ward(), request.city());

        BoardingHouse house = BoardingHouse.builder()
                .landlord(landlord)
                .name(request.name())
                .address(request.address())
                .ward(request.ward())
                .city(request.city())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .description(request.description())
                .build();

        houseRepository.save(house);
        return toResponse(house);
    }

    @Transactional
    public BoardingHouseResponse update(User landlord, Long houseId, BoardingHouseRequest request) {
        validateLocation(request.ward(), request.city());

        BoardingHouse house = getOwnedHouseOrThrow(landlord.getId(), houseId);

        house.setName(request.name());
        house.setAddress(request.address());
        house.setWard(request.ward());
        house.setCity(request.city());
        house.setLatitude(request.latitude());
        house.setLongitude(request.longitude());
        house.setDescription(request.description());

        houseRepository.save(house);
        return toResponse(house);
    }

    @Transactional
    public void delete(User landlord, Long houseId) {
        BoardingHouse house = getOwnedHouseOrThrow(landlord.getId(), houseId);
        house.setStatus(HouseStatus.DELETED);
        houseRepository.save(house);
    }

    @Transactional(readOnly = true)
    public List<BoardingHouseResponse> getAllHouses() {
        return houseRepository.findAll()
                .stream().filter(h -> h.getStatus() == HouseStatus.ACTIVE)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardingHouseResponse getById(Long houseId) {
        BoardingHouse house = houseRepository.findById(houseId)
                .filter(h -> h.getStatus() != HouseStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà trọ"));
        return toResponse(house);
    }

    @Transactional(readOnly = true)
    public List<BoardingHouseResponse> getMyHouses(User landlord) {
        return houseRepository.findByLandlordId(landlord.getId()).stream()
                .filter(h -> h.getStatus() != HouseStatus.DELETED)
                .map(this::toResponse)
                .toList();
    }

    private BoardingHouse getOwnedHouseOrThrow(Long landlordId, Long houseId) {
        return houseRepository.findByIdAndLandlordId(houseId, landlordId)
                .orElseThrow(() -> new ForbiddenActionException(
                        "Bạn không có quyền thao tác trên nhà trọ này hoặc nhà trọ không tồn tại"));
    }

    private void validateLocation(String ward, String city) {
        if (ward != null && city != null) {
            boolean valid = administrativeUnitRepository.existsByNameAndProvince(ward, city);
            if (!valid) {
                throw new IllegalArgumentException(
                        "Phường/xã và tỉnh/thành không khớp với danh mục hành chính hợp lệ. " +
                                "Vui lòng chọn từ danh sách gợi ý.");
            }
        }
    }

    private BoardingHouseResponse toResponse(BoardingHouse house) {
        int roomCount = (int) house.getRooms().stream()
                .filter(r -> r.getStatus() != tqkhanh.project.boardinghomeproject.entity.RoomStatus.HIDDEN)
                .count();
        return new BoardingHouseResponse(
                house.getId(), house.getName(), house.getAddress(), house.getWard(),
                house.getCity(), house.getLatitude(), house.getLongitude(),
                house.getDescription(), house.getStatus(), roomCount, house.getCreatedAt()
        );
    }
}