package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.RoomImage;
import tqkhanh.project.boardinghomeproject.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    /**
     * Mỗi dòng: [houseId, imageUrl, isThumbnail], ảnh chính xếp trước.
     */
    @Query("SELECT r.house.id, ri.imageUrl, ri.thumbnail FROM RoomImage ri JOIN ri.room r " +
            "WHERE r.house.id IN :houseIds AND r.status <> :hidden " +
            "ORDER BY ri.thumbnail DESC, ri.id ASC")
    List<Object[]> findCoverImages(@Param("houseIds") Collection<Long> houseIds,
                                   @Param("hidden") RoomStatus hidden);
}