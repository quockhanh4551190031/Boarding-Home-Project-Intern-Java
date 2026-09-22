// repository/BoardingHouseRepository.java
package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.Query;
import tqkhanh.project.boardinghomeproject.entity.BoardingHouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardingHouseRepository extends JpaRepository<BoardingHouse, Long> {
    List<BoardingHouse> findByLandlordId(Long landlordId);
    Optional<BoardingHouse> findByIdAndLandlordId(Long id, Long landlordId);

    @Query("SELECT DISTINCT h.ward FROM BoardingHouse h WHERE h.ward IS NOT NULL")
    List<String> findDistinctWards();

    @Query("SELECT DISTINCT h.city FROM BoardingHouse h WHERE h.city IS NOT NULL")
    List<String> findDistinctCities();
}