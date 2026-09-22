package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqkhanh.project.boardinghomeproject.entity.ChatbotConversation;

import java.util.List;

public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
    List<ChatbotConversation> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
