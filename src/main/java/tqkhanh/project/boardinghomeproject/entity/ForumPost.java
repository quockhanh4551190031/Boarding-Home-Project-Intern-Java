package tqkhanh.project.boardinghomeproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_room_id")
    private Room sharedRoom;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
