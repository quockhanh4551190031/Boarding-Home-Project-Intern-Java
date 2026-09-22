package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqkhanh.project.boardinghomeproject.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
