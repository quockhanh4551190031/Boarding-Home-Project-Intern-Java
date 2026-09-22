// controller/RoomController.java
package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.*;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.RoomService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RoomResponse> create(
            @AuthenticationPrincipal User landlord,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.create(landlord, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RoomResponse> update(
            @AuthenticationPrincipal User landlord,
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.update(landlord, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User landlord, @PathVariable Long id) {
        roomService.delete(landlord, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    @GetMapping("/house/{houseId}")
    public ResponseEntity<List<RoomResponse>> getByHouse(@PathVariable Long houseId) {
        return ResponseEntity.ok(roomService.getByHouse(houseId));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<RoomResponse>> getMine(@AuthenticationPrincipal User landlord) {
        return ResponseEntity.ok(roomService.getMyRooms(landlord));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RoomImageResponse> addImage(
            @AuthenticationPrincipal User landlord,
            @PathVariable Long id,
            @Valid @RequestBody RoomImageRequest request) {
        return ResponseEntity.ok(roomService.addImage(landlord, id, request));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteImage(
            @AuthenticationPrincipal User landlord,
            @PathVariable Long id,
            @PathVariable Long imageId) {
        roomService.deleteImage(landlord, id, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<RoomSearchResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) List<Long> amenityIds,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(roomService.search(
                keyword, minPrice, maxPrice, minArea, maxArea,
                city, amenityIds, sortBy, page, size
        ));
    }

    @GetMapping("/search/nearby")
    public ResponseEntity<RoomSearchResponse> searchNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<Long> amenityIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(roomService.searchNearby(
                lat,lng,radiusKm,minPrice,maxPrice,amenityIds,page,size
        ));
    }
}