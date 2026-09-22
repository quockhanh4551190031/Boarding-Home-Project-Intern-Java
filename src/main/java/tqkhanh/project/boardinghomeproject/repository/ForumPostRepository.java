package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tqkhanh.project.boardinghomeproject.entity.ForumPost;
import tqkhanh.project.boardinghomeproject.entity.PostStatus;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    Page<ForumPost> findByStatus(PostStatus status, Pageable pageable);
    Page<ForumPost> findBySharedRoomIdAndStatus(Long roomId, PostStatus status
    , Pageable pageable);
    Page<ForumPost> findAll(Pageable pageable);
}
