package tqkhanh.project.boardinghomeproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "viewing_requests",
        uniqueConstraints = @UniqueConstraint(name = "uk_viewing_active", columnNames = "active_key"),
        indexes = {
                @Index(name = "idx_viewing_tenant", columnList = "tenant_id, scheduled_at"),
                @Index(name = "idx_viewing_landlord", columnList = "landlord_id, status, scheduled_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    private User landlord;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(length = 500)
    private String message;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ViewingStatus status;

    @Column(name = "response_note", length = 300)
    private String responseNote;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * "tenantId:roomId" khi lịch còn hiệu lực, null khi đã kết thúc. Unique để chặn tạo trùng.
     */
    @Column(name = "active_key", length = 40)
    private String activeKey;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}