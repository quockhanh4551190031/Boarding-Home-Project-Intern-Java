package tqkhanh.project.boardinghomeproject.specification;

import tqkhanh.project.boardinghomeproject.entity.Role;
import tqkhanh.project.boardinghomeproject.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<User> filter(Role role, String status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null) predicates.add(cb.equal(root.get("role"), role));
            if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status));
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}