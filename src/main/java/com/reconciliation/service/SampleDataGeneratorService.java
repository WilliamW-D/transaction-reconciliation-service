package com.reconciliation.service;

import com.reconciliation.entity.InternalTransaction;
import com.reconciliation.entity.SettlementRecord;
import com.reconciliation.repository.InternalTransactionRepository;
import com.reconciliation.repository.SettlementRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SampleDataGeneratorService {

    private final InternalTransactionRepository internalRepo;
    private final SettlementRecordRepository settlementRepo;

    public SampleDataGeneratorService(InternalTransactionRepository internalRepo, SettlementRecordRepository settlementRepo) {
        this.internalRepo = internalRepo;
        this.settlementRepo = settlementRepo;
    }

    @Transactional
    public String generateSampleDataSet() {
        String batchSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String internalBatchId = "INT-DEMO-" + batchSuffix;
        String settlementBatchId = "SETTL-DEMO-" + batchSuffix;

        List<InternalTransaction> internalList = new ArrayList<>();
        List<SettlementRecord> settlementList = new ArrayList<>();

        LocalDateTime baseDate = LocalDateTime.now().minusDays(1);

        // 1. Exact Match Cases (8 pairs)
        for (int i = 1; i <= 8; i++) {
            String refId = "TXN-EXACT-" + i + "-" + batchSuffix;
            BigDecimal amt = new BigDecimal(100 * i + ".00");
            BigDecimal fee = new BigDecimal("2.50");
            LocalDateTime date = baseDate.plusMinutes(i * 15);

            internalList.add(new InternalTransaction(refId, "ACC-100" + i, amt, fee, "USD", date, "COMPLETED", "CREDIT_CARD", internalBatchId));
            settlementList.add(new SettlementRecord("STL-" + refId, refId, amt, amt.subtract(fee), fee, "USD", date.plusSeconds(30), "Stripe", settlementBatchId));
        }

        // 2. Amount Mismatch Cases (3 pairs)
        // Case A: Processor deducted higher fee
        String refAmt1 = "TXN-AMT-MISMATCH-1-" + batchSuffix;
        internalList.add(new InternalTransaction(refAmt1, "ACC-2001", new BigDecimal("450.00"), new BigDecimal("10.00"), "USD", baseDate.plusHours(2), "COMPLETED", "CREDIT_CARD", internalBatchId));
        settlementList.add(new SettlementRecord("STL-" + refAmt1, refAmt1, new BigDecimal("435.00"), new BigDecimal("420.00"), new BigDecimal("15.00"), "USD", baseDate.plusHours(2).plusMinutes(5), "Stripe", settlementBatchId));

        // Case B: Partial refund or fee variance
        String refAmt2 = "TXN-AMT-MISMATCH-2-" + batchSuffix;
        internalList.add(new InternalTransaction(refAmt2, "ACC-2002", new BigDecimal("1200.00"), new BigDecimal("25.00"), "USD", baseDate.plusHours(3), "COMPLETED", "WIRE", internalBatchId));
        settlementList.add(new SettlementRecord("STL-" + refAmt2, refAmt2, new BigDecimal("1150.00"), new BigDecimal("1125.00"), new BigDecimal("25.00"), "USD", baseDate.plusHours(3).plusMinutes(10), "Square", settlementBatchId));

        // 3. Date Mismatch Cases (2 pairs - date difference > 24 hrs)
        String refDate1 = "TXN-DATE-MISMATCH-1-" + batchSuffix;
        LocalDateTime dateInt = baseDate.minusDays(5);
        LocalDateTime dateSettl = baseDate; // 5 days apart
        internalList.add(new InternalTransaction(refDate1, "ACC-3001", new BigDecimal("350.00"), new BigDecimal("7.00"), "USD", dateInt, "COMPLETED", "ACH", internalBatchId));
        settlementList.add(new SettlementRecord("STL-" + refDate1, refDate1, new BigDecimal("350.00"), new BigDecimal("343.00"), new BigDecimal("7.00"), "USD", dateSettl, "Chase", settlementBatchId));

        // 4. Missing Settlement Records (Unsettled internal sales) (3 records)
        for (int i = 1; i <= 3; i++) {
            String refId = "TXN-MISSING-SETTL-" + i + "-" + batchSuffix;
            internalList.add(new InternalTransaction(refId, "ACC-400" + i, new BigDecimal(75 * i + ".50"), new BigDecimal("1.50"), "USD", baseDate.plusHours(4 + i), "COMPLETED", "CREDIT_CARD", internalBatchId));
        }

        // 5. Missing Internal Records (Unrecorded settlements / unknown deposits) (3 records)
        for (int i = 1; i <= 3; i++) {
            String stlRefId = "STL-UNRECORDED-" + i + "-" + batchSuffix;
            String intRefId = "TXN-UNKNOWN-INT-" + i + "-" + batchSuffix;
            settlementList.add(new SettlementRecord(stlRefId, intRefId, new BigDecimal(95 * i + ".00"), new BigDecimal(90 * i + ".00"), new BigDecimal("5.00"), "USD", baseDate.plusHours(5 + i), "Adyen", settlementBatchId));
        }

        // 6. Duplicate Records (1 duplicate internal, 1 duplicate settlement)
        String dupRef = "TXN-DUPLICATE-1-" + batchSuffix;
        InternalTransaction origTx = new InternalTransaction(dupRef, "ACC-5001", new BigDecimal("299.99"), new BigDecimal("6.00"), "USD", baseDate.plusHours(10), "COMPLETED", "CREDIT_CARD", internalBatchId);
        InternalTransaction dupTx = new InternalTransaction(dupRef, "ACC-5001", new BigDecimal("299.99"), new BigDecimal("6.00"), "USD", baseDate.plusHours(10), "COMPLETED", "CREDIT_CARD", internalBatchId);
        dupTx.setDuplicate(true);
        internalList.add(origTx);
        internalList.add(dupTx);

        SettlementRecord origSt = new SettlementRecord("STL-" + dupRef, dupRef, new BigDecimal("299.99"), new BigDecimal("293.99"), new BigDecimal("6.00"), "USD", baseDate.plusHours(10).plusMinutes(2), "Stripe", settlementBatchId);
        settlementList.add(origSt);

        internalRepo.saveAll(internalList);
        settlementRepo.saveAll(settlementList);

        return String.format("Sample dataset generated successfully! Internal batch: %s (%d records), Settlement batch: %s (%d records)",
                internalBatchId, internalList.size(), settlementBatchId, settlementList.size());
    }

    public String generateSampleInternalCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("reference_id,account_number,amount,fee_amount,currency,transaction_date,status,payment_method\n");
        sb.append("TXN-CSV-1001,ACC-8801,150.00,3.00,USD,2026-10-01 10:00:00,COMPLETED,CREDIT_CARD\n");
        sb.append("TXN-CSV-1002,ACC-8802,250.50,5.00,USD,2026-10-01 11:30:00,COMPLETED,CREDIT_CARD\n");
        sb.append("TXN-CSV-1003,ACC-8803,500.00,10.00,USD,2026-10-01 14:15:00,COMPLETED,ACH\n");
        sb.append("TXN-CSV-1004,ACC-8804,1200.00,24.00,USD,2026-10-01 16:00:00,COMPLETED,WIRE\n");
        sb.append("TXN-CSV-1005,ACC-8805,75.25,1.50,USD,2026-10-01 18:45:00,COMPLETED,CREDIT_CARD\n");
        return sb.toString();
    }

    public String generateSampleSettlementCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("settlement_ref_id,internal_ref_id,settlement_amount,net_amount,fee_amount,currency,settlement_date,processor_name\n");
        sb.append("STL-CSV-1001,TXN-CSV-1001,150.00,147.00,3.00,USD,2026-10-01 10:05:00,Stripe\n");
        sb.append("STL-CSV-1002,TXN-CSV-1002,240.00,235.00,5.00,USD,2026-10-01 11:35:00,Stripe\n");
        sb.append("STL-CSV-1003,TXN-CSV-1003,500.00,490.00,10.00,USD,2026-10-01 14:20:00,Chase\n");
        sb.append("STL-CSV-1006,TXN-CSV-9999,99.00,94.00,5.00,USD,2026-10-01 19:00:00,Adyen\n");
        return sb.toString();
    }
}
