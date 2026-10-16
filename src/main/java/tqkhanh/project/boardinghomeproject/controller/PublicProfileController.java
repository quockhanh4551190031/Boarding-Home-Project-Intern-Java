package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.PublicProfileResponse;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.PublicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    @GetMapping("/{id}/public-profile")
    public ResponseEntity<PublicProfileResponse> get(
            @AuthenticationPrincipal User viewer, @PathVariable Long id) {
        return ResponseEntity.ok(publicProfileService.get(viewer, id));
    }
}