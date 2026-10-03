package com.reconciliation.repository;

import com.reconciliation.entity.SettlementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, Long> {
    List<SettlementRecord> findByBatchId(String batchId);
    Optional<SettlementRecord> findFirstByInternalRefIdOrderByCreatedAtDesc(String internalRefId);
    List<SettlementRecord> findByInternalRefId(String internalRefId);
    List<SettlementRecord> findBySettlementRefId(String settlementRefId);
    long countByBatchId(String batchId);
}
