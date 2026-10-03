package com.reconciliation.repository;

import com.reconciliation.entity.InternalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternalTransactionRepository extends JpaRepository<InternalTransaction, Long> {
    List<InternalTransaction> findByBatchId(String batchId);
    Optional<InternalTransaction> findFirstByReferenceIdOrderByCreatedAtDesc(String referenceId);
    List<InternalTransaction> findByReferenceId(String referenceId);
    long countByBatchId(String batchId);
}
