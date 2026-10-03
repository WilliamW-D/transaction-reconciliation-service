package com.reconciliation.controller;

import com.reconciliation.dto.ToleranceConfigDto;
import com.reconciliation.entity.ToleranceConfig;
import com.reconciliation.service.ToleranceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tolerances")
@Tag(name = "Matching Tolerance Configuration", description = "Endpoints for viewing and updating configurable matching rules and thresholds")
public class ToleranceConfigController {

    private final ToleranceConfigService configService;

    public ToleranceConfigController(ToleranceConfigService configService) {
        this.configService = configService;
    }

    @Operation(summary = "Get all tolerance configurations")
    @GetMapping
    public ResponseEntity<List<ToleranceConfig>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @Operation(summary = "Get current active default tolerance configuration")
    @GetMapping("/default")
    public ResponseEntity<ToleranceConfig> getDefaultConfig() {
        return ResponseEntity.ok(configService.getDefaultOrCreateConfig());
    }

    @Operation(summary = "Get tolerance configuration by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ToleranceConfig> getConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(configService.getConfigById(id));
    }

    @Operation(summary = "Create a new matching tolerance configuration")
    @PostMapping
    public ResponseEntity<ToleranceConfig> createConfig(@Valid @RequestBody ToleranceConfigDto dto) {
        return ResponseEntity.ok(configService.createConfig(dto));
    }

    @Operation(summary = "Update matching tolerance thresholds")
    @PutMapping("/{id}")
    public ResponseEntity<ToleranceConfig> updateConfig(@PathVariable Long id, @Valid @RequestBody ToleranceConfigDto dto) {
        return ResponseEntity.ok(configService.updateConfig(id, dto));
    }
}
