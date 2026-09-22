package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.PostStatus;
import tqkhanh.project.boardinghomeproject.entity.ReportStatus;
import tqkhanh.project.boardinghomeproject.entity.Role;
import tqkhanh.project.boardinghomeproject.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getUsers(role, status, keyword, page, size));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getPosts(
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getPosts(status, page, size));
    }

    @PutMapping("/posts/{id}/status")
    public ResponseEntity<Void> updatePostStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostStatusRequest request) {
        adminService.updatePostStatus(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getReports(status, page, size));
    }

    @PutMapping("/reports/{id}")
    public ResponseEntity<AdminReportResponse> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusRequest request) {
        return ResponseEntity.ok(adminService.updateReportStatus(id, request));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}