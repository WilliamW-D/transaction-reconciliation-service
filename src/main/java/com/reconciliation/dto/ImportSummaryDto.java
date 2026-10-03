package com.reconciliation.dto;

public class ImportSummaryDto {
    private int recordsImported;
    private int duplicateCount;
    private String batchId;
    private String message;

    public ImportSummaryDto() {}

    public ImportSummaryDto(int recordsImported, int duplicateCount, String batchId, String message) {
        this.recordsImported = recordsImported;
        this.duplicateCount = duplicateCount;
        this.batchId = batchId;
        this.message = message;
    }

    public int getRecordsImported() { return recordsImported; }
    public void setRecordsImported(int recordsImported) { this.recordsImported = recordsImported; }

    public int getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
