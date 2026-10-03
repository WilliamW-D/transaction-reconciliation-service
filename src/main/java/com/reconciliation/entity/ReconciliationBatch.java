package com.reconciliation.entity;

import com.reconciliation.enums.BatchStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reconciliation_batches")
public class ReconciliationBatch {

    @Id
    @Column(name = "id", length = 100, nullable = false)
    private String id;

    @Column(name = "batch_name", nullable = false)
    private String batchName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status = BatchStatus.PENDING;

    @Column(name = "total_internal_count")
    private int totalInternalCount;

    @Column(name = "total_settlement_count")
    private int totalSettlementCount;

    @Column(name = "matched_count")
    private int matchedCount;

    @Column(name = "mismatch_count")
    private int mismatchCount;

    @Column(name = "missing_internal_count")
    private int missingInternalCount;

    @Column(name = "missing_settlement_count")
    private int missingSettlementCount;

    @Column(name = "duplicate_count")
    private int duplicateCount;

    @Column(name = "total_discrepancy_amount", precision = 19, scale = 4)
    private BigDecimal totalDiscrepancyAmount = BigDecimal.ZERO;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "tolerance_config_summary", length = 500)
    private String toleranceConfigSummary;

    public ReconciliationBatch() {}

    public ReconciliationBatch(String id, String batchName) {
        this.id = id;
        this.batchName = batchName;
        this.startedAt = LocalDateTime.now();
        this.status = BatchStatus.PENDING;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public int getTotalInternalCount() { return totalInternalCount; }
    public void setTotalInternalCount(int totalInternalCount) { this.totalInternalCount = totalInternalCount; }

    public int getTotalSettlementCount() { return totalSettlementCount; }
    public void setTotalSettlementCount(int totalSettlementCount) { this.totalSettlementCount = totalSettlementCount; }

    public int getMatchedCount() { return matchedCount; }
    public void setMatchedCount(int matchedCount) { this.matchedCount = matchedCount; }

    public int getMismatchCount() { return mismatchCount; }
    public void setMismatchCount(int mismatchCount) { this.mismatchCount = mismatchCount; }

    public int getMissingInternalCount() { return missingInternalCount; }
    public void setMissingInternalCount(int missingInternalCount) { this.missingInternalCount = missingInternalCount; }

    public int getMissingSettlementCount() { return missingSettlementCount; }
    public void setMissingSettlementCount(int missingSettlementCount) { this.missingSettlementCount = missingSettlementCount; }

    public int getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }

    public BigDecimal getTotalDiscrepancyAmount() { return totalDiscrepancyAmount; }
    public void setTotalDiscrepancyAmount(BigDecimal totalDiscrepancyAmount) { this.totalDiscrepancyAmount = totalDiscrepancyAmount; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getToleranceConfigSummary() { return toleranceConfigSummary; }
    public void setToleranceConfigSummary(String toleranceConfigSummary) { this.toleranceConfigSummary = toleranceConfigSummary; }
}
