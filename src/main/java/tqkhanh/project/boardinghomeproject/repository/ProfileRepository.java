package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.domain.Pageable;
import tqkhanh.project.boardinghomeproject.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tqkhanh.project.boardinghomeproject.entity.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    @Query("SELECT COUNT(p) > 0 FROM Profile p WHERE p.phone = :phone AND p.user.id <> :userId")
    boolean existsByPhoneAndUserIdNot(@Param("phone") String phone, @Param("userId") Long userId);

    @Query("""
            SELECT p FROM Profile p JOIN FETCH p.user u
            WHERE u.status = 'ACTIVE' AND u.role IN :roles
              AND p.fullName IS NOT NULL AND LOWER(p.fullName) LIKE :pattern ESCAPE '!'
            ORDER BY CASE WHEN LOWER(p.fullName) LIKE :prefix ESCAPE '!' THEN 0 ELSE 1 END, p.fullName
            """)
    List<Profile> searchByName(@Param("roles") Collection<Role> roles,
                               @Param("pattern") String pattern,
                               @Param("prefix") String prefix,
                               Pageable pageable);
}