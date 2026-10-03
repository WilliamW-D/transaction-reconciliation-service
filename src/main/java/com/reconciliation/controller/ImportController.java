package com.reconciliation.controller;

import com.reconciliation.dto.ImportSummaryDto;
import com.reconciliation.service.CsvParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/imports")
@Tag(name = "Transaction Imports", description = "Endpoints for importing internal sales and external settlement records via CSV")
public class ImportController {

    private final CsvParserService csvParserService;

    public ImportController(CsvParserService csvParserService) {
        this.csvParserService = csvParserService;
    }

    @Operation(summary = "Import internal payment transactions CSV file")
    @PostMapping(value = "/internal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportSummaryDto> importInternalFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "batchId", required = false) String batchId) {
        try {
            ImportSummaryDto summary = csvParserService.parseAndSaveInternalTransactions(file.getInputStream(), batchId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            throw new RuntimeException("Error reading uploaded file: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "Import internal payment transactions from raw CSV text")
    @PostMapping(value = "/internal/raw", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ImportSummaryDto> importInternalRaw(
            @RequestBody String csvText,
            @RequestParam(value = "batchId", required = false) String batchId) {
        ByteArrayInputStream is = new ByteArrayInputStream(csvText.getBytes(StandardCharsets.UTF_8));
        ImportSummaryDto summary = csvParserService.parseAndSaveInternalTransactions(is, batchId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Import external settlement records CSV file")
    @PostMapping(value = "/settlement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportSummaryDto> importSettlementFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "batchId", required = false) String batchId) {
        try {
            ImportSummaryDto summary = csvParserService.parseAndSaveSettlementRecords(file.getInputStream(), batchId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            throw new RuntimeException("Error reading uploaded file: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "Import external settlement records from raw CSV text")
    @PostMapping(value = "/settlement/raw", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ImportSummaryDto> importSettlementRaw(
            @RequestBody String csvText,
            @RequestParam(value = "batchId", required = false) String batchId) {
        ByteArrayInputStream is = new ByteArrayInputStream(csvText.getBytes(StandardCharsets.UTF_8));
        ImportSummaryDto summary = csvParserService.parseAndSaveSettlementRecords(is, batchId);
        return ResponseEntity.ok(summary);
    }
}
