package com.reconciliation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_records", indexes = {
    @Index(name = "idx_settlement_internal_ref_id", columnList = "internal_ref_id"),
    @Index(name = "idx_settlement_ref_id", columnList = "settlement_ref_id"),
    @Index(name = "idx_settlement_batch_id", columnList = "batch_id")
})
public class SettlementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_ref_id", nullable = false)
    private String settlementRefId;

    @Column(name = "internal_ref_id", nullable = false)
    private String internalRefId;

    @Column(name = "settlement_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal settlementAmount;

    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "fee_amount", precision = 19, scale = 4)
    private BigDecimal feeAmount;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "settlement_date", nullable = false)
    private LocalDateTime settlementDate;

    @Column(name = "processor_name", length = 50)
    private String processorName;

    @Column(name = "is_duplicate", nullable = false)
    private boolean isDuplicate = false;

    @Column(name = "batch_id", length = 100)
    private String batchId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SettlementRecord() {}

    public SettlementRecord(String settlementRefId, String internalRefId, BigDecimal settlementAmount, 
                            BigDecimal netAmount, BigDecimal feeAmount, String currency, 
                            LocalDateTime settlementDate, String processorName, String batchId) {
        this.settlementRefId = settlementRefId;
        this.internalRefId = internalRefId;
        this.settlementAmount = settlementAmount;
        this.netAmount = netAmount;
        this.feeAmount = feeAmount != null ? feeAmount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "USD";
        this.settlementDate = settlementDate;
        this.processorName = processorName;
        this.batchId = batchId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSettlementRefId() { return settlementRefId; }
    public void setSettlementRefId(String settlementRefId) { this.settlementRefId = settlementRefId; }

    public String getInternalRefId() { return internalRefId; }
    public void setInternalRefId(String internalRefId) { this.internalRefId = internalRefId; }

    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal settlementAmount) { this.settlementAmount = settlementAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDateTime settlementDate) { this.settlementDate = settlementDate; }

    public String getProcessorName() { return processorName; }
    public void setProcessorName(String processorName) { this.processorName = processorName; }

    public boolean isDuplicate() { return isDuplicate; }
    public void setDuplicate(boolean duplicate) { isDuplicate = duplicate; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
