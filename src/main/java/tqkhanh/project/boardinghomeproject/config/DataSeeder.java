package tqkhanh.project.boardinghomeproject.config;

import tqkhanh.project.boardinghomeproject.entity.*;
import tqkhanh.project.boardinghomeproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardingHouseRepository houseRepository;
    private final RoomRepository roomRepository;
    private final AdministrativeUnitRepository administrativeUnitRepository;

    @Override
    public void run(String... args) {
        seedAmenities();
        seedHousesAndRooms();
    }

    private void seedAmenities() {
        if (amenityRepository.count() == 0) {
            List<String> defaults = List.of(
                    "Wifi", "Máy lạnh", "Gác lửng", "Giờ giấc tự do",
                    "Chỗ để xe", "Máy giặt chung", "Ban công", "Cửa sổ", "Nội thất cơ bản"
            );
            defaults.forEach(name -> amenityRepository.save(Amenity.builder().name(name).build()));
        }
    }

    private void seedHousesAndRooms() {
        if (roomRepository.count() > 0) return; // đã có dữ liệu thật, không ghi đè

        List<Amenity> amenities = amenityRepository.findAll();
        if (amenities.isEmpty()) return;

        User landlord1 = getOrCreateLandlord("landlord1.demo@gmail.com", "Nguyễn Văn Chủ");
        User landlord2 = getOrCreateLandlord("landlord2.demo@gmail.com", "Trần Thị Trọ");

        seedHouse(landlord1, "Thành phố Hồ Chí Minh", "Nhà trọ Bình An",
                10.7756, 106.7019, amenities, new Object[][]{
                        {"Phòng đơn tiện nghi", 2500000, 18, 1, "Phòng nhỏ gọn, đầy đủ tiện nghi cơ bản, gần trường đại học"},
                        {"Phòng đôi rộng rãi", 3800000, 28, 2, "Phòng rộng, có gác lửng, phù hợp ở ghép 2 người"},
                        {"Phòng studio full nội thất", 5500000, 32, 2, "Nội thất đầy đủ, máy lạnh, tủ lạnh, máy giặt riêng"}
                });

        seedHouse(landlord1, "Thành phố Hà Nội", "Nhà trọ Hòa Bình",
                21.0285, 105.8542, amenities, new Object[][]{
                        {"Phòng trọ sinh viên", 2200000, 16, 1, "Gần các trường đại học lớn, an ninh tốt"},
                        {"Phòng có ban công", 3200000, 22, 2, "View đẹp, thoáng mát, có ban công riêng"}
                });

        seedHouse(landlord2, "Thành phố Đà Nẵng", "Nhà trọ Sông Hàn",
                16.0544, 108.2022, amenities, new Object[][]{
                        {"Phòng gần biển", 3000000, 20, 2, "Cách biển Mỹ Khê 5 phút đi xe, thoáng mát"},
                        {"Phòng cao cấp full nội thất", 4500000, 25, 2, "Đầy đủ nội thất cao cấp, an ninh 24/7"},
                        {"Phòng tiết kiệm", 1800000, 14, 1, "Phù hợp sinh viên, giá rẻ, gần chợ"}
                });
    }

    private User getOrCreateLandlord(String email, String name) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.LANDLORD)
                    .build();
            return userRepository.save(user);
        });
    }

    private void seedHouse(User landlord, String province, String houseName,
                           double baseLat, double baseLng, List<Amenity> amenities,
                           Object[][] roomData) {

        List<AdministrativeUnit> wardsInProvince = administrativeUnitRepository.findAll().stream()
                .filter(u -> u.getProvince().equals(province))
                .toList();

        if (wardsInProvince.isEmpty()) return; // chưa seed bảng administrative_units, bỏ qua

        String ward = wardsInProvince.get(new Random().nextInt(wardsInProvince.size())).getName();

        BoardingHouse house = BoardingHouse.builder()
                .landlord(landlord)
                .name(houseName)
                .address("123 Đường Demo, " + ward)
                .ward(ward)
                .city(province)
                .latitude(BigDecimal.valueOf(baseLat))
                .longitude(BigDecimal.valueOf(baseLng))
                .description("Nhà trọ demo dùng để test dữ liệu frontend")
                .status(HouseStatus.ACTIVE)
                .build();

        houseRepository.save(house);

        for (Object[] r : roomData) {
            Room room = Room.builder()
                    .house(house)
                    .title((String) r[0])
                    .price(BigDecimal.valueOf((int) r[1]))
                    .area(BigDecimal.valueOf((int) r[2]))
                    .maxOccupants((int) r[3])
                    .description((String) r[4])
                    .status(RoomStatus.AVAILABLE)
                    .amenities(pickRandomAmenities(amenities))
                    .build();

            List<RoomImage> images = List.of(
                    RoomImage.builder()
                            .imageUrl("https://picsum.photos/seed/" + Math.abs(room.getTitle().hashCode()) + "a/640/480")
                            .thumbnail(true)
                            .room(room)
                            .build(),
                    RoomImage.builder()
                            .imageUrl("https://picsum.photos/seed/" + Math.abs(room.getTitle().hashCode()) + "b/640/480")
                            .thumbnail(false)
                            .room(room)
                            .build()
            );
            room.setImages(new ArrayList<>(images));

            roomRepository.save(room);
        }
    }

    private Set<Amenity> pickRandomAmenities(List<Amenity> amenities) {
        List<Amenity> shuffled = new ArrayList<>(amenities);
        Collections.shuffle(shuffled);
        int count = 2 + new Random().nextInt(3); // 2-4 tiện ích ngẫu nhiên
        return new HashSet<>(shuffled.subList(0, Math.min(count, shuffled.size())));
    }
}