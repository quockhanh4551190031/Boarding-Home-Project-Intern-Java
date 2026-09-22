package tqkhanh.project.boardinghomeproject.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tqkhanh.project.boardinghomeproject.dto.ProfileResponse;
import tqkhanh.project.boardinghomeproject.dto.ProfileUpdateRequest;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ProfileService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/me/profile")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(profileService.getMyProfile(currentUser));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProfileUpdateRequest request
            ) {
        return ResponseEntity.ok(profileService.updateMyProfile(currentUser,request));
    }
}
