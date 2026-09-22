package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.FavoriteResponse;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.FavoriteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{roomId}")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @AuthenticationPrincipal User user, @PathVariable Long roomId) {
        return ResponseEntity.ok(favoriteService.addFavorite(user, roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal User user, @PathVariable Long roomId) {
        favoriteService.removeFavorite(user, roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getMyFavorites(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(favoriteService.getMyFavorites(user));
    }

    @GetMapping("/{roomId}/status")
    public ResponseEntity<Map<String, Boolean>> checkStatus(
            @AuthenticationPrincipal User user, @PathVariable Long roomId) {
        return ResponseEntity.ok(favoriteService.isFavorited(user, roomId));
    }
}