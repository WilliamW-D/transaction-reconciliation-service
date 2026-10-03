package com.reconciliation.dto;

public class ReconciliationRequestDto {
    private String batchName;
    private Long toleranceConfigId;
    private String internalBatchId;
    private String settlementBatchId;

    public ReconciliationRequestDto() {}

    public ReconciliationRequestDto(String batchName, Long toleranceConfigId, String internalBatchId, String settlementBatchId) {
        this.batchName = batchName;
        this.toleranceConfigId = toleranceConfigId;
        this.internalBatchId = internalBatchId;
        this.settlementBatchId = settlementBatchId;
    }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public Long getToleranceConfigId() { return toleranceConfigId; }
    public void setToleranceConfigId(Long toleranceConfigId) { this.toleranceConfigId = toleranceConfigId; }

    public String getInternalBatchId() { return internalBatchId; }
    public void setInternalBatchId(String internalBatchId) { this.internalBatchId = internalBatchId; }

    public String getSettlementBatchId() { return settlementBatchId; }
    public void setSettlementBatchId(String settlementBatchId) { this.settlementBatchId = settlementBatchId; }
}
