package com.reconciliation.service;

import com.reconciliation.dto.DiscrepancyResolutionDto;
import com.reconciliation.dto.ReconciliationRequestDto;
import com.reconciliation.entity.*;
import com.reconciliation.enums.AuditAction;
import com.reconciliation.enums.BatchStatus;
import com.reconciliation.enums.MatchStatus;
import com.reconciliation.exception.ReconciliationException;
import com.reconciliation.exception.ResourceNotFoundException;
import com.reconciliation.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ReconciliationService {

    private final ReconciliationBatchRepository batchRepo;
    private final MatchResultRepository matchResultRepo;
    private final InternalTransactionRepository internalRepo;
    private final SettlementRecordRepository settlementRepo;
    private final ToleranceConfigService toleranceConfigService;
    private final MatchingEngineService matchingEngine;
    private final AuditService auditService;

    public ReconciliationService(ReconciliationBatchRepository batchRepo,
                                 MatchResultRepository matchResultRepo,
                                 InternalTransactionRepository internalRepo,
                                 SettlementRecordRepository settlementRepo,
                                 ToleranceConfigService toleranceConfigService,
                                 MatchingEngineService matchingEngine,
                                 AuditService auditService) {
        this.batchRepo = batchRepo;
        this.matchResultRepo = matchResultRepo;
        this.internalRepo = internalRepo;
        this.settlementRepo = settlementRepo;
        this.toleranceConfigService = toleranceConfigService;
        this.matchingEngine = matchingEngine;
        this.auditService = auditService;
    }

    @Transactional
    public ReconciliationBatch runReconciliation(ReconciliationRequestDto request) {
        String batchId = "RECON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String batchName = (request.getBatchName() != null && !request.getBatchName().isBlank()) 
                ? request.getBatchName() 
                : "Reconciliation Run " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        ReconciliationBatch batch = new ReconciliationBatch(batchId, batchName);
        batch.setStatus(BatchStatus.PROCESSING);

        ToleranceConfig config = (request.getToleranceConfigId() != null)
                ? toleranceConfigService.getConfigById(request.getToleranceConfigId())
                : toleranceConfigService.getDefaultOrCreateConfig();

        batch.setToleranceConfigSummary(String.format("Config: %s (AmountTol: $%s, FeeTol: $%s, DateWindow: %dmins)",
                config.getName(), config.getAmountTolerance(), config.getFeeTolerance(), config.getDateWindowMinutes()));

        batchRepo.save(batch);

        auditService.log("ReconciliationBatch", batchId, AuditAction.BATCH_STARTED,
                "Started reconciliation batch " + batchName, "USER");

        try {
            List<InternalTransaction> internalList = (request.getInternalBatchId() != null && !request.getInternalBatchId().isBlank())
                    ? internalRepo.findByBatchId(request.getInternalBatchId())
                    : internalRepo.findAll();

            List<SettlementRecord> settlementList = (request.getSettlementBatchId() != null && !request.getSettlementBatchId().isBlank())
                    ? settlementRepo.findByBatchId(request.getSettlementBatchId())
                    : settlementRepo.findAll();

            if (internalList.isEmpty() && settlementList.isEmpty()) {
                throw new ReconciliationException("No internal or settlement transactions found to reconcile.");
            }

            List<MatchResult> results = matchingEngine.executeMatching(internalList, settlementList, config, batchId);

            matchResultRepo.saveAll(results);

            // Compute statistics
            int matched = 0;
            int mismatch = 0;
            int missingInternal = 0;
            int missingSettlement = 0;
            int duplicate = 0;
            BigDecimal totalDiscrepancyAmount = BigDecimal.ZERO;

            for (MatchResult res : results) {
                if (res.getMatchStatus() == MatchStatus.MATCHED) {
                    matched++;
                } else if (res.getMatchStatus() == MatchStatus.DISCREPANCY) {
                    mismatch++;
                    totalDiscrepancyAmount = totalDiscrepancyAmount.add(res.getAmountDifference());
                } else if (res.getMatchStatus() == MatchStatus.MISSING_INTERNAL) {
                    missingInternal++;
                    totalDiscrepancyAmount = totalDiscrepancyAmount.add(res.getAmountDifference());
                } else if (res.getMatchStatus() == MatchStatus.MISSING_SETTLEMENT) {
                    missingSettlement++;
                    totalDiscrepancyAmount = totalDiscrepancyAmount.add(res.getAmountDifference());
                } else if (res.getMatchStatus() == MatchStatus.DUPLICATE) {
                    duplicate++;
                }
            }

            batch.setTotalInternalCount(internalList.size());
            batch.setTotalSettlementCount(settlementList.size());
            batch.setMatchedCount(matched);
            batch.setMismatchCount(mismatch);
            batch.setMissingInternalCount(missingInternal);
            batch.setMissingSettlementCount(missingSettlement);
            batch.setDuplicateCount(duplicate);
            batch.setTotalDiscrepancyAmount(totalDiscrepancyAmount);
            batch.setStatus(BatchStatus.COMPLETED);
            batch.setCompletedAt(LocalDateTime.now());

            ReconciliationBatch savedBatch = batchRepo.save(batch);

            auditService.log("ReconciliationBatch", batchId, AuditAction.BATCH_COMPLETED,
                    String.format("Batch completed: %d matched, %d mismatches, %d missing int, %d missing settl, %d duplicates. Total discrepancy: $%s",
                            matched, mismatch, missingInternal, missingSettlement, duplicate, totalDiscrepancyAmount),
                    "USER");

            return savedBatch;

        } catch (Exception e) {
            batch.setStatus(BatchStatus.FAILED);
            batch.setCompletedAt(LocalDateTime.now());
            batchRepo.save(batch);
            throw new ReconciliationException("Reconciliation failed: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public ReconciliationBatch getBatch(String batchId) {
        return batchRepo.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("ReconciliationBatch not found: " + batchId));
    }

    @Transactional(readOnly = true)
    public List<ReconciliationBatch> getAllBatches() {
        return batchRepo.findAllByOrderByStartedAtDesc();
    }

    @Transactional(readOnly = true)
    public Page<MatchResult> getBatchResults(String batchId, MatchStatus status, Pageable pageable) {
        if (status != null) {
            return matchResultRepo.findByBatchIdAndMatchStatus(batchId, status, pageable);
        }
        return matchResultRepo.findByBatchId(batchId, pageable);
    }

    @Transactional
    public MatchResult resolveDiscrepancy(Long resultId, DiscrepancyResolutionDto resolutionDto) {
        MatchResult result = matchResultRepo.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult not found with id: " + resultId));

        result.setResolved(true);
        result.setResolutionNotes(resolutionDto.getResolutionNotes());
        result.setResolvedBy(resolutionDto.getResolvedBy() != null ? resolutionDto.getResolvedBy() : "USER");

        MatchResult updated = matchResultRepo.save(result);

        auditService.log("MatchResult", resultId.toString(), AuditAction.DISCREPANCY_RESOLVED,
                "Resolved discrepancy for batch " + result.getBatchId() + ": " + resolutionDto.getResolutionNotes(),
                result.getResolvedBy());

        return updated;
    }

    @Transactional(readOnly = true)
    public byte[] generateCsvReport(String batchId) {
        ReconciliationBatch batch = getBatch(batchId);
        List<MatchResult> results = matchResultRepo.findByBatchId(batchId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // CSV Header
        writer.println("Batch ID,Match Status,Discrepancy Category,Internal Ref ID,Settlement Ref ID,Internal Amount,Settlement Amount,Amount Difference,Fee Difference,Resolved,Notes");

        for (MatchResult res : results) {
            String internalRef = (res.getInternalTransaction() != null) ? res.getInternalTransaction().getReferenceId() : "N/A";
            String settlementRef = (res.getSettlementRecord() != null) ? res.getSettlementRecord().getSettlementRefId() : "N/A";
            String internalAmt = (res.getInternalTransaction() != null) ? res.getInternalTransaction().getAmount().toString() : "0.00";
            String settlementAmt = (res.getSettlementRecord() != null) ? res.getSettlementRecord().getSettlementAmount().toString() : "0.00";

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    res.getBatchId(),
                    res.getMatchStatus(),
                    res.getDiscrepancyCategory(),
                    internalRef,
                    settlementRef,
                    internalAmt,
                    settlementAmt,
                    res.getAmountDifference(),
                    res.getFeeDifference(),
                    res.isResolved() ? "YES" : "NO",
                    res.getNotes() != null ? res.getNotes().replace("\"", "'") : ""
            );
        }

        writer.flush();
        auditService.log("ReconciliationBatch", batchId, AuditAction.EXPORT_REPORT,
                "Exported CSV reconciliation report for batch " + batchId, "USER");

        return out.toByteArray();
    }
}
