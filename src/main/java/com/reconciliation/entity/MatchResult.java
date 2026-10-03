package com.reconciliation.entity;

import com.reconciliation.enums.DiscrepancyCategory;
import com.reconciliation.enums.MatchStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_results", indexes = {
    @Index(name = "idx_match_batch_id", columnList = "batch_id"),
    @Index(name = "idx_match_status", columnList = "match_status"),
    @Index(name = "idx_discrepancy_category", columnList = "discrepancy_category")
})
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false, length = 100)
    private String batchId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "internal_transaction_id")
    private InternalTransaction internalTransaction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "settlement_record_id")
    private SettlementRecord settlementRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 30)
    private MatchStatus matchStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "discrepancy_category", nullable = false, length = 30)
    private DiscrepancyCategory discrepancyCategory;

    @Column(name = "amount_difference", precision = 19, scale = 4)
    private BigDecimal amountDifference = BigDecimal.ZERO;

    @Column(name = "fee_difference", precision = 19, scale = 4)
    private BigDecimal feeDifference = BigDecimal.ZERO;

    @Column(name = "date_difference_seconds")
    private Long dateDifferenceSeconds = 0L;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MatchResult() {}

    public MatchResult(String batchId, InternalTransaction internalTransaction, SettlementRecord settlementRecord,
                       MatchStatus matchStatus, DiscrepancyCategory discrepancyCategory,
                       BigDecimal amountDifference, BigDecimal feeDifference, Long dateDifferenceSeconds, String notes) {
        this.batchId = batchId;
        this.internalTransaction = internalTransaction;
        this.settlementRecord = settlementRecord;
        this.matchStatus = matchStatus;
        this.discrepancyCategory = discrepancyCategory;
        this.amountDifference = amountDifference != null ? amountDifference : BigDecimal.ZERO;
        this.feeDifference = feeDifference != null ? feeDifference : BigDecimal.ZERO;
        this.dateDifferenceSeconds = dateDifferenceSeconds != null ? dateDifferenceSeconds : 0L;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public InternalTransaction getInternalTransaction() { return internalTransaction; }
    public void setInternalTransaction(InternalTransaction internalTransaction) { this.internalTransaction = internalTransaction; }

    public SettlementRecord getSettlementRecord() { return settlementRecord; }
    public void setSettlementRecord(SettlementRecord settlementRecord) { this.settlementRecord = settlementRecord; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public DiscrepancyCategory getDiscrepancyCategory() { return discrepancyCategory; }
    public void setDiscrepancyCategory(DiscrepancyCategory discrepancyCategory) { this.discrepancyCategory = discrepancyCategory; }

    public BigDecimal getAmountDifference() { return amountDifference; }
    public void setAmountDifference(BigDecimal amountDifference) { this.amountDifference = amountDifference; }

    public BigDecimal getFeeDifference() { return feeDifference; }
    public void setFeeDifference(BigDecimal feeDifference) { this.feeDifference = feeDifference; }

    public Long getDateDifferenceSeconds() { return dateDifferenceSeconds; }
    public void setDateDifferenceSeconds(Long dateDifferenceSeconds) { this.dateDifferenceSeconds = dateDifferenceSeconds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
