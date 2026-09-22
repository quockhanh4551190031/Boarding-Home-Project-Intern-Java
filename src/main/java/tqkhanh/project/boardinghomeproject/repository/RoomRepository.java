// repository/RoomRepository.java
package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tqkhanh.project.boardinghomeproject.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    List<Room> findByHouseId(Long houseId);

    @Query("SELECT r FROM Room r WHERE r.id = :roomId AND r.house.landlord.id = :landlordId")
    Optional<Room> findByIdAndLandlordId(@Param("roomId") Long roomId, @Param("landlordId") Long landlordId);

    @Query("SELECT r FROM Room r WHERE r.house.landlord.id = :landlordId")
    List<Room> findAllByLandlordId(@Param("landlordId") Long landlordId);
}