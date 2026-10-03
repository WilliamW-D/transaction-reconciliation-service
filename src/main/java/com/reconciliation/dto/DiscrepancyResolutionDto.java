package com.reconciliation.dto;

import jakarta.validation.constraints.NotBlank;

public class DiscrepancyResolutionDto {
    
    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
    
    private String resolvedBy;

    public DiscrepancyResolutionDto() {}

    public DiscrepancyResolutionDto(String resolutionNotes, String resolvedBy) {
        this.resolutionNotes = resolutionNotes;
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
}
