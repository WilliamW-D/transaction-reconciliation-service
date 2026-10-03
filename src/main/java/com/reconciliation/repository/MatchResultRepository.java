package com.reconciliation.repository;

import com.reconciliation.entity.MatchResult;
import com.reconciliation.enums.DiscrepancyCategory;
import com.reconciliation.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByBatchId(String batchId);
    Page<MatchResult> findByBatchId(String batchId, Pageable pageable);
    
    Page<MatchResult> findByBatchIdAndMatchStatus(String batchId, MatchStatus matchStatus, Pageable pageable);
    Page<MatchResult> findByBatchIdAndDiscrepancyCategory(String batchId, DiscrepancyCategory category, Pageable pageable);
    
    List<MatchResult> findByBatchIdAndMatchStatus(String batchId, MatchStatus matchStatus);
    long countByBatchIdAndMatchStatus(String batchId, MatchStatus matchStatus);
}
