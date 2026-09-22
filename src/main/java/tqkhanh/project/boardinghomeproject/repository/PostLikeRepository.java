package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqkhanh.project.boardinghomeproject.entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.PostLikeId> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
