package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tqkhanh.project.boardinghomeproject.entity.ForumPost;
import tqkhanh.project.boardinghomeproject.entity.PostStatus;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    Page<ForumPost> findByStatus(PostStatus status, Pageable pageable);

    Page<ForumPost> findBySharedRoomIdAndStatus(Long roomId, PostStatus status
            , Pageable pageable);

    Page<ForumPost> findAll(Pageable pageable);

    long countByUserIdAndStatus(Long userId, PostStatus status);

    @Query("""
            SELECT p FROM ForumPost p JOIN FETCH p.user u
            WHERE p.status = :status
              AND (LOWER(p.title) LIKE :pattern ESCAPE '!' OR LOWER(p.content) LIKE :pattern ESCAPE '!')
            ORDER BY CASE WHEN LOWER(p.title) LIKE :prefix ESCAPE '!' THEN 0
                          WHEN LOWER(p.title) LIKE :pattern ESCAPE '!' THEN 1
                          ELSE 2 END,
                     p.createdAt DESC
            """)
    List<ForumPost> search(@Param("status") PostStatus status,
                           @Param("pattern") String pattern,
                           @Param("prefix") String prefix,
                           Pageable pageable);
}
