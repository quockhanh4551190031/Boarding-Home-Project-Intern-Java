package tqkhanh.project.boardinghomeproject.entity;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "post_likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PostLike.PostLikeId.class)
public class PostLike {
    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostLikeId implements Serializable {
        private Long postId;
        private Long userId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PostLikeId that)) return false;

            return Objects.equals(postId, that.postId) && Objects.equals(userId, that.userId);

        }

        @Override
        public int hashCode() {
            return Objects.hash(postId,userId);
        }
    }
}
