package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqkhanh.project.boardinghomeproject.entity.Amenity;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
}
