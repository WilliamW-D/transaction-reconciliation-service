package com.reconciliation.service;

import com.reconciliation.dto.ImportSummaryDto;
import com.reconciliation.entity.InternalTransaction;
import com.reconciliation.entity.SettlementRecord;
import com.reconciliation.enums.AuditAction;
import com.reconciliation.exception.ReconciliationException;
import com.reconciliation.repository.InternalTransactionRepository;
import com.reconciliation.repository.SettlementRecordRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CsvParserService {

    private final InternalTransactionRepository internalRepo;
    private final SettlementRecordRepository settlementRepo;
    private final AuditService auditService;

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    public CsvParserService(InternalTransactionRepository internalRepo, 
                            SettlementRecordRepository settlementRepo, 
                            AuditService auditService) {
        this.internalRepo = internalRepo;
        this.settlementRepo = settlementRepo;
        this.auditService = auditService;
    }

    @Transactional
    public ImportSummaryDto parseAndSaveInternalTransactions(InputStream inputStream, String batchId) {
        if (batchId == null || batchId.isBlank()) {
            batchId = "INT-BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        List<InternalTransaction> transactionsToSave = new ArrayList<>();
        Set<String> seenRefIds = new HashSet<>();
        int duplicateCount = 0;
        int importedCount = 0;

        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .setIgnoreHeaderCase(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : csvParser) {
                String refId = getHeaderValue(record, "reference_id", "referenceId", "ref_id", "transaction_id");
                if (refId == null || refId.isBlank()) {
                    continue; // Skip invalid rows without reference ID
                }

                String account = getHeaderValue(record, "account_number", "accountNumber", "account");
                BigDecimal amount = parseBigDecimal(getHeaderValue(record, "amount", "transaction_amount"));
                BigDecimal fee = parseBigDecimal(getHeaderValue(record, "fee_amount", "fee"));
                String currency = getHeaderValueOrDefault(record, "USD", "currency");
                LocalDateTime txDate = parseDateTime(getHeaderValue(record, "transaction_date", "date", "timestamp"));
                String status = getHeaderValueOrDefault(record, "COMPLETED", "status");
                String paymentMethod = getHeaderValueOrDefault(record, "CREDIT_CARD", "payment_method", "method");

                InternalTransaction tx = new InternalTransaction(refId, account, amount, fee, currency, txDate, status, paymentMethod, batchId);

                // Duplicate Detection (within batch or DB)
                if (seenRefIds.contains(refId) || !internalRepo.findByReferenceId(refId).isEmpty()) {
                    tx.setDuplicate(true);
                    duplicateCount++;
                } else {
                    seenRefIds.add(refId);
                }

                transactionsToSave.add(tx);
                importedCount++;
            }

            internalRepo.saveAll(transactionsToSave);

            auditService.log("InternalTransaction", batchId, AuditAction.IMPORT_INTERNAL,
                    String.format("Imported %d internal records (%d duplicates detected) for batch %s", importedCount, duplicateCount, batchId),
                    "USER");

            return new ImportSummaryDto(importedCount, duplicateCount, batchId, "Internal transaction import completed successfully");

        } catch (Exception e) {
            throw new ReconciliationException("Failed to parse internal transactions CSV: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ImportSummaryDto parseAndSaveSettlementRecords(InputStream inputStream, String batchId) {
        if (batchId == null || batchId.isBlank()) {
            batchId = "SETTL-BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        List<SettlementRecord> recordsToSave = new ArrayList<>();
        Set<String> seenSettlementRefIds = new HashSet<>();
        int duplicateCount = 0;
        int importedCount = 0;

        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .setIgnoreHeaderCase(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : csvParser) {
                String settlementRefId = getHeaderValue(record, "settlement_ref_id", "settlement_id", "id");
                String internalRefId = getHeaderValue(record, "internal_ref_id", "reference_id", "ref_id", "transaction_id");

                if ((settlementRefId == null || settlementRefId.isBlank()) && (internalRefId == null || internalRefId.isBlank())) {
                    continue;
                }

                if (settlementRefId == null || settlementRefId.isBlank()) {
                    settlementRefId = "SETTL-" + UUID.randomUUID().toString().substring(0, 8);
                }
                if (internalRefId == null || internalRefId.isBlank()) {
                    internalRefId = "UNKNOWN";
                }

                BigDecimal settlementAmount = parseBigDecimal(getHeaderValue(record, "settlement_amount", "amount", "gross_amount"));
                BigDecimal netAmount = parseBigDecimal(getHeaderValue(record, "net_amount", "net"));
                BigDecimal feeAmount = parseBigDecimal(getHeaderValue(record, "fee_amount", "fee", "processor_fee"));
                String currency = getHeaderValueOrDefault(record, "USD", "currency");
                LocalDateTime date = parseDateTime(getHeaderValue(record, "settlement_date", "date", "timestamp"));
                String processor = getHeaderValueOrDefault(record, "Stripe", "processor_name", "processor", "gateway");

                SettlementRecord settlement = new SettlementRecord(settlementRefId, internalRefId, settlementAmount, netAmount, feeAmount, currency, date, processor, batchId);

                // Duplicate check
                if (seenSettlementRefIds.contains(settlementRefId) || !settlementRepo.findBySettlementRefId(settlementRefId).isEmpty()) {
                    settlement.setDuplicate(true);
                    duplicateCount++;
                } else {
                    seenSettlementRefIds.add(settlementRefId);
                }

                recordsToSave.add(settlement);
                importedCount++;
            }

            settlementRepo.saveAll(recordsToSave);

            auditService.log("SettlementRecord", batchId, AuditAction.IMPORT_SETTLEMENT,
                    String.format("Imported %d settlement records (%d duplicates detected) for batch %s", importedCount, duplicateCount, batchId),
                    "USER");

            return new ImportSummaryDto(importedCount, duplicateCount, batchId, "Settlement record import completed successfully");

        } catch (Exception e) {
            throw new ReconciliationException("Failed to parse settlement records CSV: " + e.getMessage(), e);
        }
    }

    private String getHeaderValue(CSVRecord record, String... headerCandidates) {
        for (String candidate : headerCandidates) {
            if (record.isMapped(candidate) && record.get(candidate) != null && !record.get(candidate).isBlank()) {
                return record.get(candidate).trim();
            }
        }
        return null;
    }

    private String getHeaderValueOrDefault(CSVRecord record, String defaultValue, String... headerCandidates) {
        String val = getHeaderValue(record, headerCandidates);
        return val != null ? val : defaultValue;
    }

    private BigDecimal parseBigDecimal(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        String clean = raw.replaceAll("[^0-9.-]", "");
        if (clean.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return LocalDateTime.now();
        String clean = raw.trim();

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(clean, formatter);
            } catch (Exception ignored) {
                try {
                    LocalDate dateOnly = LocalDate.parse(clean, formatter);
                    return dateOnly.atStartOfDay();
                } catch (Exception ignored2) {}
            }
        }
        return LocalDateTime.now();
    }
}
