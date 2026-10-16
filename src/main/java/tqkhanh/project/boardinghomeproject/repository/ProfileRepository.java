package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    @Query("SELECT COUNT(p) > 0 FROM Profile p WHERE p.phone = :phone AND p.user.id <> :userId")
    boolean existsByPhoneAndUserIdNot(@Param("phone") String phone, @Param("userId") Long userId);
}