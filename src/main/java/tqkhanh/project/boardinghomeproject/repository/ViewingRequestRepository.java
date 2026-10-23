package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.ViewingRequest;
import tqkhanh.project.boardinghomeproject.entity.ViewingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ViewingRequestRepository extends JpaRepository<ViewingRequest, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ViewingRequest v WHERE v.id = :id")
    Optional<ViewingRequest> findByIdForUpdate(@Param("id") Long id);

    Optional<ViewingRequest> findByActiveKey(String activeKey);

    long countByTenantIdAndStatusAndScheduledAtGreaterThanEqual(Long tenantId, ViewingStatus status, LocalDateTime from);

    long countByTenantIdAndCreatedAtAfter(Long tenantId, LocalDateTime since);

    long countByLandlordIdAndStatusAndScheduledAtGreaterThanEqual(Long landlordId, ViewingStatus status, LocalDateTime from);

    /**
     * Giải phóng khóa của các lịch đã quá giờ để người thuê đặt lại được phòng đó.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ViewingRequest v SET v.activeKey = NULL " +
            "WHERE v.tenant.id = :tenantId AND v.activeKey IS NOT NULL AND v.scheduledAt < :now")
    int releaseStaleKeys(@Param("tenantId") Long tenantId, @Param("now") LocalDateTime now);

    @Query("SELECT v.scheduledAt FROM ViewingRequest v " +
            "WHERE v.landlord.id = :landlordId AND v.status = :accepted AND v.scheduledAt >= :now")
    List<LocalDateTime> acceptedTimes(@Param("landlordId") Long landlordId,
                                      @Param("accepted") ViewingStatus accepted,
                                      @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"room", "room.house", "tenant", "landlord"})
    @Query("SELECT v FROM ViewingRequest v WHERE v.tenant.id = :userId AND v.status IN :statuses AND v.scheduledAt >= :now")
    Page<ViewingRequest> tenantActive(@Param("userId") Long userId, @Param("statuses") Collection<ViewingStatus> statuses,
                                      @Param("now") LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"room", "room.house", "tenant", "landlord"})
    @Query("SELECT v FROM ViewingRequest v WHERE v.tenant.id = :userId AND (v.status NOT IN :active OR v.scheduledAt < :now)")
    Page<ViewingRequest> tenantHistory(@Param("userId") Long userId, @Param("active") Collection<ViewingStatus> active,
                                       @Param("now") LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"room", "room.house", "tenant", "landlord"})
    @Query("SELECT v FROM ViewingRequest v WHERE v.landlord.id = :userId AND v.status IN :statuses AND v.scheduledAt >= :now")
    Page<ViewingRequest> landlordActive(@Param("userId") Long userId, @Param("statuses") Collection<ViewingStatus> statuses,
                                        @Param("now") LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"room", "room.house", "tenant", "landlord"})
    @Query("SELECT v FROM ViewingRequest v WHERE v.landlord.id = :userId AND (v.status NOT IN :active OR v.scheduledAt < :now)")
    Page<ViewingRequest> landlordHistory(@Param("userId") Long userId, @Param("active") Collection<ViewingStatus> active,
                                         @Param("now") LocalDateTime now, Pageable pageable);
}