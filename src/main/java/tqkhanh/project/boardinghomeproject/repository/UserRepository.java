package tqkhanh.project.boardinghomeproject.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tqkhanh.project.boardinghomeproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}