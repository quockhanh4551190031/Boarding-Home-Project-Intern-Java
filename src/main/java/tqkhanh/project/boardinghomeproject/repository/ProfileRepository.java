package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqkhanh.project.boardinghomeproject.entity.Profile;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);
    boolean existsByPhone(String phone);
}
