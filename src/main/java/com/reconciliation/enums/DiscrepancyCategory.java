package com.reconciliation.enums;

public enum DiscrepancyCategory {
    EXACT_MATCH,
    AMOUNT_MISMATCH,
    FEE_MISMATCH,
    DATE_MISMATCH,
    MISSING_INTERNAL,
    MISSING_SETTLEMENT,
    DUPLICATE_TRANSACTION,
    UNMATCHED
}
