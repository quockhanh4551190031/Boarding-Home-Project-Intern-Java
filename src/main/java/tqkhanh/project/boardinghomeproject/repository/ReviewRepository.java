package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.Review;
import tqkhanh.project.boardinghomeproject.entity.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndTargetTypeAndTargetId(Long userId, ReviewTargetType type, Long targetId);

    Page<Review> findByTargetTypeAndTargetIdOrderByCreatedAtDescIdDesc(
            ReviewTargetType type, Long targetId, Pageable pageable);

    long countByUserIdAndUpdatedAtAfter(Long userId, LocalDateTime since);

    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.targetType = :type AND r.targetId = :targetId GROUP BY r.rating")
    List<Object[]> countByRating(@Param("type") ReviewTargetType type, @Param("targetId") Long targetId);

    @Query("SELECT r.targetId, COUNT(r), AVG(r.rating) FROM Review r " +
            "WHERE r.targetType = :type AND r.targetId IN :ids GROUP BY r.targetId")
    List<Object[]> summarize(@Param("type") ReviewTargetType type, @Param("ids") Collection<Long> ids);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.user.id = :userId AND LOWER(r.comment) = :comment " +
            "AND NOT (r.targetType = :type AND r.targetId = :targetId) AND r.updatedAt > :since")
    boolean existsDuplicateComment(@Param("userId") Long userId,
                                   @Param("comment") String comment,
                                   @Param("type") ReviewTargetType type,
                                   @Param("targetId") Long targetId,
                                   @Param("since") LocalDateTime since);
}