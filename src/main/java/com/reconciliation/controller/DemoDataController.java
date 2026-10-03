package com.reconciliation.controller;

import com.reconciliation.service.SampleDataGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
@Tag(name = "Demo Data Generator", description = "Endpoints for generating mock sample transactions and downloading sample CSV files")
public class DemoDataController {

    private final SampleDataGeneratorService generatorService;

    public DemoDataController(SampleDataGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @Operation(summary = "Generate realistic synthetic sample dataset in database")
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateSampleData() {
        String message = generatorService.generateSampleDataSet();
        return ResponseEntity.ok(Map.of("message", message, "status", "SUCCESS"));
    }

    @Operation(summary = "Download sample internal transactions CSV template")
    @GetMapping("/sample-internal-csv")
    public ResponseEntity<String> getSampleInternalCsv() {
        String csv = generatorService.generateSampleInternalCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "sample_internal_transactions.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @Operation(summary = "Download sample settlement records CSV template")
    @GetMapping("/sample-settlement-csv")
    public ResponseEntity<String> getSampleSettlementCsv() {
        String csv = generatorService.generateSampleSettlementCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "sample_settlement_records.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
