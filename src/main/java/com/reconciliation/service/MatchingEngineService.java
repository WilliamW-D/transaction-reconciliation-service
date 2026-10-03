package com.reconciliation.service;

import com.reconciliation.entity.*;
import com.reconciliation.enums.DiscrepancyCategory;
import com.reconciliation.enums.MatchStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

@Service
public class MatchingEngineService {

    public List<MatchResult> executeMatching(List<InternalTransaction> internalRecords,
                                              List<SettlementRecord> settlementRecords,
                                              ToleranceConfig config,
                                              String batchId) {
        List<MatchResult> results = new ArrayList<>();

        Set<Long> matchedInternalIds = new HashSet<>();
        Set<Long> matchedSettlementIds = new HashSet<>();

        // Map internal records by reference ID
        Map<String, List<InternalTransaction>> internalMap = new HashMap<>();
        for (InternalTransaction tx : internalRecords) {
            if (tx.isDuplicate()) {
                results.add(new MatchResult(
                        batchId, tx, null,
                        MatchStatus.DUPLICATE, DiscrepancyCategory.DUPLICATE_TRANSACTION,
                        BigDecimal.ZERO, BigDecimal.ZERO, 0L,
                        "Internal transaction marked as duplicate reference ID: " + tx.getReferenceId()
                ));
                matchedInternalIds.add(tx.getId());
            } else {
                internalMap.computeIfAbsent(tx.getReferenceId(), k -> new ArrayList<>()).add(tx);
            }
        }

        // Map settlement records by internal reference ID
        Map<String, List<SettlementRecord>> settlementMap = new HashMap<>();
        for (SettlementRecord st : settlementRecords) {
            if (st.isDuplicate()) {
                results.add(new MatchResult(
                        batchId, null, st,
                        MatchStatus.DUPLICATE, DiscrepancyCategory.DUPLICATE_TRANSACTION,
                        BigDecimal.ZERO, BigDecimal.ZERO, 0L,
                        "Settlement record marked as duplicate settlement ref ID: " + st.getSettlementRefId()
                ));
                matchedSettlementIds.add(st.getId());
            } else {
                settlementMap.computeIfAbsent(st.getInternalRefId(), k -> new ArrayList<>()).add(st);
            }
        }

        // PASS 1: Exact Reference Matching
        for (Map.Entry<String, List<InternalTransaction>> entry : internalMap.entrySet()) {
            String refId = entry.getKey();
            List<InternalTransaction> txList = entry.getValue();

            if (settlementMap.containsKey(refId)) {
                List<SettlementRecord> stList = settlementMap.get(refId);

                int pairCount = Math.min(txList.size(), stList.size());
                for (int i = 0; i < pairCount; i++) {
                    InternalTransaction tx = txList.get(i);
                    SettlementRecord st = stList.get(i);

                    matchedInternalIds.add(tx.getId());
                    matchedSettlementIds.add(st.getId());

                    MatchResult result = evaluatePair(tx, st, config, batchId);
                    results.add(result);
                }
            }
        }

        // PASS 2: Fuzzy Candidate Matching for remaining unmatched records
        List<InternalTransaction> remainingInternal = internalRecords.stream()
                .filter(tx -> !matchedInternalIds.contains(tx.getId()))
                .toList();

        List<SettlementRecord> remainingSettlement = new ArrayList<>(settlementRecords.stream()
                .filter(st -> !matchedSettlementIds.contains(st.getId()))
                .toList());

        for (InternalTransaction tx : remainingInternal) {
            SettlementRecord bestCandidate = null;
            BigDecimal minDiff = null;

            for (SettlementRecord st : remainingSettlement) {
                if (matchedSettlementIds.contains(st.getId())) continue;

                if (config.isCurrencyStrict() && !tx.getCurrency().equalsIgnoreCase(st.getCurrency())) {
                    continue;
                }

                BigDecimal amountDiff = tx.getAmount().subtract(st.getSettlementAmount()).abs();
                long dateDiffSeconds = Math.abs(Duration.between(tx.getTransactionDate(), st.getSettlementDate()).toSeconds());
                long dateDiffMinutes = dateDiffSeconds / 60;

                if (amountDiff.compareTo(config.getAmountTolerance()) <= 0 && dateDiffMinutes <= config.getDateWindowMinutes()) {
                    if (minDiff == null || amountDiff.compareTo(minDiff) < 0) {
                        minDiff = amountDiff;
                        bestCandidate = st;
                    }
                }
            }

            if (bestCandidate != null) {
                matchedInternalIds.add(tx.getId());
                matchedSettlementIds.add(bestCandidate.getId());

                MatchResult result = evaluatePair(tx, bestCandidate, config, batchId);
                result.setNotes("Fuzzy matched by amount tolerance & date window. " + (result.getNotes() != null ? result.getNotes() : ""));
                results.add(result);
            }
        }

        // PASS 3: Categorize Orphans
        for (InternalTransaction tx : internalRecords) {
            if (!matchedInternalIds.contains(tx.getId())) {
                results.add(new MatchResult(
                        batchId, tx, null,
                        MatchStatus.MISSING_SETTLEMENT, DiscrepancyCategory.MISSING_SETTLEMENT,
                        tx.getAmount(), tx.getFeeAmount(), 0L,
                        "Internal transaction has no corresponding settlement record"
                ));
            }
        }

        for (SettlementRecord st : settlementRecords) {
            if (!matchedSettlementIds.contains(st.getId())) {
                results.add(new MatchResult(
                        batchId, null, st,
                        MatchStatus.MISSING_INTERNAL, DiscrepancyCategory.MISSING_INTERNAL,
                        st.getSettlementAmount(), st.getFeeAmount(), 0L,
                        "Settlement record has no corresponding internal payment record"
                ));
            }
        }

        return results;
    }

    private MatchResult evaluatePair(InternalTransaction tx, SettlementRecord st, ToleranceConfig config, String batchId) {
        BigDecimal amountDiff = tx.getAmount().subtract(st.getSettlementAmount()).abs().setScale(4, RoundingMode.HALF_UP);
        BigDecimal feeDiff = tx.getFeeAmount().subtract(st.getFeeAmount()).abs().setScale(4, RoundingMode.HALF_UP);
        long dateDiffSeconds = Math.abs(Duration.between(tx.getTransactionDate(), st.getSettlementDate()).toSeconds());
        long dateDiffMinutes = dateDiffSeconds / 60;

        boolean currencyMatch = !config.isCurrencyStrict() || tx.getCurrency().equalsIgnoreCase(st.getCurrency());
        boolean amountWithinTolerance = amountDiff.compareTo(config.getAmountTolerance()) <= 0;
        boolean feeWithinTolerance = feeDiff.compareTo(config.getFeeTolerance()) <= 0;
        boolean dateWithinTolerance = dateDiffMinutes <= config.getDateWindowMinutes();

        if (currencyMatch && amountWithinTolerance && feeWithinTolerance && dateWithinTolerance) {
            return new MatchResult(
                    batchId, tx, st,
                    MatchStatus.MATCHED, DiscrepancyCategory.EXACT_MATCH,
                    amountDiff, feeDiff, dateDiffSeconds,
                    "Exact match within configured tolerances"
            );
        }

        // Determine specific discrepancy category
        DiscrepancyCategory category;
        String notes;

        if (!currencyMatch) {
            category = DiscrepancyCategory.AMOUNT_MISMATCH;
            notes = String.format("Currency mismatch: internal=%s, settlement=%s", tx.getCurrency(), st.getCurrency());
        } else if (!amountWithinTolerance) {
            category = DiscrepancyCategory.AMOUNT_MISMATCH;
            notes = String.format("Amount discrepancy: internal=$%s, settlement=$%s (Diff: $%s, Tolerance: $%s)",
                    tx.getAmount(), st.getSettlementAmount(), amountDiff, config.getAmountTolerance());
        } else if (!feeWithinTolerance) {
            category = DiscrepancyCategory.FEE_MISMATCH;
            notes = String.format("Processor fee discrepancy: internal fee=$%s, settlement fee=$%s (Diff: $%s, Tolerance: $%s)",
                    tx.getFeeAmount(), st.getFeeAmount(), feeDiff, config.getFeeTolerance());
        } else {
            category = DiscrepancyCategory.DATE_MISMATCH;
            notes = String.format("Transaction date window exceeded: internal=%s, settlement=%s (Diff: %d mins, Max allowed: %d mins)",
                    tx.getTransactionDate(), st.getSettlementDate(), dateDiffMinutes, config.getDateWindowMinutes());
        }

        return new MatchResult(
                batchId, tx, st,
                MatchStatus.DISCREPANCY, category,
                amountDiff, feeDiff, dateDiffSeconds, notes
        );
    }
}
