// repository/BoardingHouseRepository.java
package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tqkhanh.project.boardinghomeproject.entity.BoardingHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import tqkhanh.project.boardinghomeproject.entity.HouseStatus;

import java.util.List;
import java.util.Optional;

public interface BoardingHouseRepository extends JpaRepository<BoardingHouse, Long> {
    List<BoardingHouse> findByLandlordId(Long landlordId);

    Optional<BoardingHouse> findByIdAndLandlordId(Long id, Long landlordId);

    @Query("SELECT DISTINCT h.ward FROM BoardingHouse h WHERE h.ward IS NOT NULL")
    List<String> findDistinctWards();

    @Query("SELECT DISTINCT h.city FROM BoardingHouse h WHERE h.city IS NOT NULL")
    List<String> findDistinctCities();

    @Query("""
            SELECT h FROM BoardingHouse h
            WHERE h.status = :status AND LOWER(h.name) LIKE :pattern ESCAPE '!'
            ORDER BY CASE WHEN LOWER(h.name) LIKE :prefix ESCAPE '!' THEN 0 ELSE 1 END, h.name
            """)
    List<BoardingHouse> searchByName(@Param("status") HouseStatus status,
                                     @Param("pattern") String pattern,
                                     @Param("prefix") String prefix,
                                     Pageable pageable);
}