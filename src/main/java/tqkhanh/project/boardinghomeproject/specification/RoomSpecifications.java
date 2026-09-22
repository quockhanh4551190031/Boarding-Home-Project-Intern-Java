package tqkhanh.project.boardinghomeproject.specification;

import tqkhanh.project.boardinghomeproject.entity.Amenity;
import tqkhanh.project.boardinghomeproject.entity.BoardingHouse;
import tqkhanh.project.boardinghomeproject.entity.Room;
import tqkhanh.project.boardinghomeproject.entity.RoomStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RoomSpecifications {
    private RoomSpecifications() {}

    public static Specification<Room> filter(
            String keyword,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minArea, BigDecimal maxArea,
            String city,
            List<Long> amenityIds
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), RoomStatus.AVAILABLE));

            Join<Room, BoardingHouse> house = root.join("house", JoinType.LEFT);

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(house.get("name")), pattern),
                        cb.like(cb.lower(house.get("address")), pattern),
                        cb.like(cb.lower(house.get("ward")), pattern)
                ));
            }

            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (minArea != null) predicates.add(cb.greaterThanOrEqualTo(root.get("area"), minArea));
            if (maxArea != null) predicates.add(cb.lessThanOrEqualTo(root.get("area"), maxArea));

            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(cb.lower(house.get("city")), city.toLowerCase()));
            }

            if (amenityIds != null && !amenityIds.isEmpty()) {
                query.distinct(true);
                Join<Room, Amenity> amenities = root.join("amenities", JoinType.INNER);
                predicates.add(amenities.get("id").in(amenityIds));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
