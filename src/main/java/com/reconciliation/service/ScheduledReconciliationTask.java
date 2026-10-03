package com.reconciliation.service;

import com.reconciliation.dto.ReconciliationRequestDto;
import com.reconciliation.entity.ReconciliationBatch;
import com.reconciliation.repository.InternalTransactionRepository;
import com.reconciliation.repository.SettlementRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledReconciliationTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduledReconciliationTask.class);

    private final ReconciliationService reconciliationService;
    private final InternalTransactionRepository internalRepo;
    private final SettlementRecordRepository settlementRepo;

    public ScheduledReconciliationTask(ReconciliationService reconciliationService,
                                      InternalTransactionRepository internalRepo,
                                      SettlementRecordRepository settlementRepo) {
        this.reconciliationService = reconciliationService;
        this.internalRepo = internalRepo;
        this.settlementRepo = settlementRepo;
    }

    // Runs auto-reconciliation periodically (e.g. every 10 minutes, or on startup check)
    @Scheduled(cron = "0 */10 * * * *")
    public void runScheduledAutoReconciliation() {
        long internalCount = internalRepo.count();
        long settlementCount = settlementRepo.count();

        if (internalCount > 0 || settlementCount > 0) {
            log.info("Scheduled auto-reconciliation triggered for {} internal records and {} settlement records.",
                    internalCount, settlementCount);
            try {
                ReconciliationRequestDto req = new ReconciliationRequestDto();
                req.setBatchName("Scheduled Auto-Reconciliation");
                ReconciliationBatch batch = reconciliationService.runReconciliation(req);
                log.info("Scheduled auto-reconciliation completed successfully. Batch ID: {}", batch.getId());
            } catch (Exception e) {
                log.error("Scheduled auto-reconciliation execution failed: {}", e.getMessage());
            }
        } else {
            log.debug("Scheduled auto-reconciliation skipped - no transaction records found.");
        }
    }
}
