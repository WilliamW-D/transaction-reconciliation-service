package com.reconciliation.controller;

import com.reconciliation.dto.DiscrepancyResolutionDto;
import com.reconciliation.dto.ReconciliationRequestDto;
import com.reconciliation.entity.MatchResult;
import com.reconciliation.entity.ReconciliationBatch;
import com.reconciliation.enums.MatchStatus;
import com.reconciliation.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Reconciliation Engine", description = "Endpoints for triggering reconciliation, retrieving results, resolving discrepancies, and exporting reports")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Operation(summary = "Trigger automatic transaction reconciliation batch")
    @PostMapping("/run")
    public ResponseEntity<ReconciliationBatch> runReconciliation(@RequestBody(required = false) ReconciliationRequestDto request) {
        if (request == null) {
            request = new ReconciliationRequestDto();
        }
        ReconciliationBatch batch = reconciliationService.runReconciliation(request);
        return ResponseEntity.ok(batch);
    }

    @Operation(summary = "Get all reconciliation batches")
    @GetMapping("/batches")
    public ResponseEntity<List<ReconciliationBatch>> getAllBatches() {
        return ResponseEntity.ok(reconciliationService.getAllBatches());
    }

    @Operation(summary = "Get specific reconciliation batch details")
    @GetMapping("/batches/{batchId}")
    public ResponseEntity<ReconciliationBatch> getBatch(@PathVariable String batchId) {
        return ResponseEntity.ok(reconciliationService.getBatch(batchId));
    }

    @Operation(summary = "Get paginated match results for a batch")
    @GetMapping("/batches/{batchId}/results")
    public ResponseEntity<Page<MatchResult>> getBatchResults(
            @PathVariable String batchId,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        return ResponseEntity.ok(reconciliationService.getBatchResults(batchId, status, pageRequest));
    }

    @Operation(summary = "Resolve a discrepancy item with audit notes")
    @PostMapping("/results/{id}/resolve")
    public ResponseEntity<MatchResult> resolveDiscrepancy(
            @PathVariable Long id,
            @Valid @RequestBody DiscrepancyResolutionDto resolutionDto) {
        MatchResult result = reconciliationService.resolveDiscrepancy(id, resolutionDto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Export reconciliation report in CSV format")
    @GetMapping("/batches/{batchId}/export")
    public ResponseEntity<byte[]> exportBatchCsv(@PathVariable String batchId) {
        byte[] csvData = reconciliationService.generateCsvReport(batchId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "reconciliation_report_" + batchId + ".csv");
        return ResponseEntity.ok().headers(headers).body(csvData);
    }
}
