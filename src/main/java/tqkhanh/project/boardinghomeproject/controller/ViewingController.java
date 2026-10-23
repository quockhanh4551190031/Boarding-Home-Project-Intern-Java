package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.ViewingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/viewings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ViewingController {

    private final ViewingService viewingService;

    @PostMapping
    public ResponseEntity<ViewingResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody CreateViewingRequest request) {
        return ResponseEntity.ok(viewingService.create(user, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<ViewingListResponse> mine(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "upcoming") String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(viewingService.mine(user, scope, page, size));
    }

    @GetMapping("/incoming")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ViewingListResponse> incoming(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "pending") String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(viewingService.incoming(user, scope, page, size));
    }

    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> pendingCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("count", viewingService.pendingCount(user)));
    }

    @GetMapping("/room/{roomId}/state")
    public ResponseEntity<ViewingRoomState> state(@AuthenticationPrincipal User user, @PathVariable Long roomId) {
        return ResponseEntity.ok(viewingService.state(user, roomId));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ViewingResponse> accept(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(viewingService.accept(user, id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ViewingResponse> reject(
            @AuthenticationPrincipal User user, @PathVariable Long id,
            @Valid @RequestBody(required = false) ViewingNoteRequest request) {
        return ResponseEntity.ok(viewingService.reject(user, id, request == null ? null : request.note()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ViewingResponse> cancel(
            @AuthenticationPrincipal User user, @PathVariable Long id,
            @Valid @RequestBody(required = false) ViewingNoteRequest request) {
        return ResponseEntity.ok(viewingService.cancel(user, id, request == null ? null : request.note()));
    }
}