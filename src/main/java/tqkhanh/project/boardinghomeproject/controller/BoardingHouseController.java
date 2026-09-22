// controller/BoardingHouseController.java
package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.BoardingHouseRequest;
import tqkhanh.project.boardinghomeproject.dto.BoardingHouseResponse;
import tqkhanh.project.boardinghomeproject.entity.User;
import tqkhanh.project.boardinghomeproject.service.BoardingHouseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/houses")
@RequiredArgsConstructor
public class BoardingHouseController {

    private final BoardingHouseService houseService;

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BoardingHouseResponse> create(
            @AuthenticationPrincipal User landlord,
            @Valid @RequestBody BoardingHouseRequest request) {
        return ResponseEntity.ok(houseService.create(landlord, request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BoardingHouseResponse>> getAll() {
        return ResponseEntity.ok(houseService.getAllHouses());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BoardingHouseResponse> update(
            @AuthenticationPrincipal User landlord,
            @PathVariable Long id,
            @Valid @RequestBody BoardingHouseRequest request) {
        return ResponseEntity.ok(houseService.update(landlord, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User landlord, @PathVariable Long id) {
        houseService.delete(landlord, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardingHouseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(houseService.getById(id));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('LANDLORD')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<BoardingHouseResponse>> getMine(@AuthenticationPrincipal User landlord) {
        return ResponseEntity.ok(houseService.getMyHouses(landlord));
    }
}