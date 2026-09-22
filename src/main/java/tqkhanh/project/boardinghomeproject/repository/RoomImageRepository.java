// repository/RoomImageRepository.java
package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
}