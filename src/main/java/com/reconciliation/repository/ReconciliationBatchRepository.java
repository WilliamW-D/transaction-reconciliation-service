package com.reconciliation.repository;

import com.reconciliation.entity.ReconciliationBatch;
import com.reconciliation.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationBatchRepository extends JpaRepository<ReconciliationBatch, String> {
    List<ReconciliationBatch> findByStatusOrderByStartedAtDesc(BatchStatus status);
    List<ReconciliationBatch> findAllByOrderByStartedAtDesc();
}
