package tqkhanh.project.boardinghomeproject.controller;

import tqkhanh.project.boardinghomeproject.repository.AdministrativeUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final AdministrativeUnitRepository administrativeUnitRepository;

    @GetMapping("/provinces")
    public ResponseEntity<List<String>> getProvinces() {
        return ResponseEntity.ok(administrativeUnitRepository.findDistinctProvinces());
    }

    @GetMapping("/wards")
    public ResponseEntity<List<String>> getWards(@RequestParam String province) {
        return ResponseEntity.ok(administrativeUnitRepository.findNamesByProvince(province));
    }
}