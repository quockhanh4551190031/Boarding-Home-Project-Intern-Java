// repository/MessageRepository.java
package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    long countByChatRoomIdAndIsReadFalseAndSenderIdNot(Long chatRoomId, Long excludedSenderId);
    @Query("SELECT COUNT(m) FROM Message m WHERE m.isRead = false AND m.sender.id <> :userId " +
            "AND (m.chatRoom.user1.id = :userId OR m.chatRoom.user2.id = :userId)")
    long countUnreadForUser(@Param("userId") Long userId);
}