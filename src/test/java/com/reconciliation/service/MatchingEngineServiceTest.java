package com.reconciliation.service;

import com.reconciliation.entity.InternalTransaction;
import com.reconciliation.entity.MatchResult;
import com.reconciliation.entity.SettlementRecord;
import com.reconciliation.entity.ToleranceConfig;
import com.reconciliation.enums.DiscrepancyCategory;
import com.reconciliation.enums.MatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineServiceTest {

    private MatchingEngineService matchingEngine;
    private ToleranceConfig toleranceConfig;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngineService();
        toleranceConfig = new ToleranceConfig("Test Config", new BigDecimal("0.05"), new BigDecimal("0.01"), 1440, true, true);
    }

    @Test
    @DisplayName("Should produce EXACT_MATCH when internal and settlement records match perfectly")
    void testExactMatch() {
        LocalDateTime now = LocalDateTime.now();
        InternalTransaction tx = new InternalTransaction("TXN-100", "ACC-1", new BigDecimal("100.00"), new BigDecimal("2.50"), "USD", now, "COMPLETED", "CREDIT_CARD", "BATCH-1");
        SettlementRecord st = new SettlementRecord("STL-100", "TXN-100", new BigDecimal("100.00"), new BigDecimal("97.50"), new BigDecimal("2.50"), "USD", now, "Stripe", "BATCH-1");

        List<MatchResult> results = matchingEngine.executeMatching(List.of(tx), List.of(st), toleranceConfig, "RECON-1");

        assertEquals(1, results.size());
        MatchResult res = results.get(0);
        assertEquals(MatchStatus.MATCHED, res.getMatchStatus());
        assertEquals(DiscrepancyCategory.EXACT_MATCH, res.getDiscrepancyCategory());
        assertEquals(BigDecimal.ZERO.setScale(4), res.getAmountDifference());
    }

    @Test
    @DisplayName("Should detect AMOUNT_MISMATCH when settlement amount exceeds tolerance threshold")
    void testAmountMismatch() {
        LocalDateTime now = LocalDateTime.now();
        InternalTransaction tx = new InternalTransaction("TXN-101", "ACC-1", new BigDecimal("100.00"), new BigDecimal("2.50"), "USD", now, "COMPLETED", "CREDIT_CARD", "BATCH-1");
        SettlementRecord st = new SettlementRecord("STL-101", "TXN-101", new BigDecimal("90.00"), new BigDecimal("87.50"), new BigDecimal("2.50"), "USD", now, "Stripe", "BATCH-1");

        List<MatchResult> results = matchingEngine.executeMatching(List.of(tx), List.of(st), toleranceConfig, "RECON-1");

        assertEquals(1, results.size());
        MatchResult res = results.get(0);
        assertEquals(MatchStatus.DISCREPANCY, res.getMatchStatus());
        assertEquals(DiscrepancyCategory.AMOUNT_MISMATCH, res.getDiscrepancyCategory());
        assertEquals(new BigDecimal("10.0000"), res.getAmountDifference());
    }

    @Test
    @DisplayName("Should categorize orphan internal transaction as MISSING_SETTLEMENT")
    void testMissingSettlement() {
        LocalDateTime now = LocalDateTime.now();
        InternalTransaction tx = new InternalTransaction("TXN-ORPHAN", "ACC-1", new BigDecimal("50.00"), new BigDecimal("1.00"), "USD", now, "COMPLETED", "CREDIT_CARD", "BATCH-1");

        List<MatchResult> results = matchingEngine.executeMatching(List.of(tx), List.of(), toleranceConfig, "RECON-1");

        assertEquals(1, results.size());
        MatchResult res = results.get(0);
        assertEquals(MatchStatus.MISSING_SETTLEMENT, res.getMatchStatus());
        assertEquals(DiscrepancyCategory.MISSING_SETTLEMENT, res.getDiscrepancyCategory());
    }

    @Test
    @DisplayName("Should categorize orphan settlement record as MISSING_INTERNAL")
    void testMissingInternal() {
        LocalDateTime now = LocalDateTime.now();
        SettlementRecord st = new SettlementRecord("STL-ORPHAN", "TXN-UNKNOWN", new BigDecimal("75.00"), new BigDecimal("73.00"), new BigDecimal("2.00"), "USD", now, "Adyen", "BATCH-1");

        List<MatchResult> results = matchingEngine.executeMatching(List.of(), List.of(st), toleranceConfig, "RECON-1");

        assertEquals(1, results.size());
        MatchResult res = results.get(0);
        assertEquals(MatchStatus.MISSING_INTERNAL, res.getMatchStatus());
        assertEquals(DiscrepancyCategory.MISSING_INTERNAL, res.getDiscrepancyCategory());
    }
}
