// repository/ChatRoomRepository.java
package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c WHERE " +
            "(c.user1.id = :userA AND c.user2.id = :userB) OR " +
            "(c.user1.id = :userB AND c.user2.id = :userA)")
    Optional<ChatRoom> findBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("SELECT c FROM ChatRoom c WHERE c.user1.id = :userId OR c.user2.id = :userId " +
            "ORDER BY c.createdAt DESC")
    List<ChatRoom> findAllForUser(@Param("userId") Long userId);
}