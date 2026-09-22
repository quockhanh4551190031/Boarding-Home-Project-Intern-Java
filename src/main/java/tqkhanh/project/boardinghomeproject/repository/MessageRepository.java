// repository/MessageRepository.java
package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    long countByChatRoomIdAndIsReadFalseAndSenderIdNot(Long chatRoomId, Long excludedSenderId);
}