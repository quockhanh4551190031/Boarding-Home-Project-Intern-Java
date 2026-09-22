package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.dto.AmenityResponse;
import tqkhanh.project.boardinghomeproject.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityRepository amenityRepository;

    @GetMapping
    public ResponseEntity<List<AmenityResponse>> getAll() {
        List<AmenityResponse> result = amenityRepository.findAll().stream()
                .map(a -> new AmenityResponse(a.getId(), a.getName(), a.getIcon()))
                .toList();
        return ResponseEntity.ok(result);
    }
}