package com.reconciliation.service;

import com.reconciliation.dto.ToleranceConfigDto;
import com.reconciliation.entity.ToleranceConfig;
import com.reconciliation.enums.AuditAction;
import com.reconciliation.exception.ResourceNotFoundException;
import com.reconciliation.repository.ToleranceConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ToleranceConfigService {

    private final ToleranceConfigRepository toleranceConfigRepository;
    private final AuditService auditService;

    public ToleranceConfigService(ToleranceConfigRepository toleranceConfigRepository, AuditService auditService) {
        this.toleranceConfigRepository = toleranceConfigRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ToleranceConfig getDefaultOrCreateConfig() {
        return toleranceConfigRepository.findFirstByIsDefaultTrue()
                .orElseGet(() -> {
                    ToleranceConfig defaultConfig = new ToleranceConfig(
                            "Default Rules",
                            new BigDecimal("0.05"),
                            new BigDecimal("0.01"),
                            1440, // 24 hours
                            true,
                            true
                    );
                    return toleranceConfigRepository.save(defaultConfig);
                });
    }

    @Transactional(readOnly = true)
    public List<ToleranceConfig> getAllConfigs() {
        return toleranceConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ToleranceConfig getConfigById(Long id) {
        return toleranceConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ToleranceConfig not found with id: " + id));
    }

    @Transactional
    public ToleranceConfig createConfig(ToleranceConfigDto dto) {
        if (dto.isDefault()) {
            clearPreviousDefaults();
        }
        ToleranceConfig config = new ToleranceConfig(
                dto.getName(),
                dto.getAmountTolerance() != null ? dto.getAmountTolerance() : BigDecimal.ZERO,
                dto.getFeeTolerance() != null ? dto.getFeeTolerance() : BigDecimal.ZERO,
                dto.getDateWindowMinutes() > 0 ? dto.getDateWindowMinutes() : 1440,
                dto.isCurrencyStrict(),
                dto.isDefault()
        );
        ToleranceConfig saved = toleranceConfigRepository.save(config);

        auditService.log("ToleranceConfig", saved.getId().toString(), AuditAction.TOLERANCE_UPDATED,
                String.format("Created tolerance config '%s': AmountTol=$%s, FeeTol=$%s, DateWindow=%dmins",
                        saved.getName(), saved.getAmountTolerance(), saved.getFeeTolerance(), saved.getDateWindowMinutes()),
                "USER");

        return saved;
    }

    @Transactional
    public ToleranceConfig updateConfig(Long id, ToleranceConfigDto dto) {
        ToleranceConfig config = getConfigById(id);
        if (dto.isDefault() && !config.isDefault()) {
            clearPreviousDefaults();
        }

        config.setName(dto.getName());
        config.setAmountTolerance(dto.getAmountTolerance());
        config.setFeeTolerance(dto.getFeeTolerance());
        config.setDateWindowMinutes(dto.getDateWindowMinutes());
        config.setCurrencyStrict(dto.isCurrencyStrict());
        config.setDefault(dto.isDefault());
        config.setUpdatedAt(LocalDateTime.now());

        ToleranceConfig updated = toleranceConfigRepository.save(config);

        auditService.log("ToleranceConfig", updated.getId().toString(), AuditAction.TOLERANCE_UPDATED,
                String.format("Updated tolerance config '%s': AmountTol=$%s, FeeTol=$%s, DateWindow=%dmins",
                        updated.getName(), updated.getAmountTolerance(), updated.getFeeTolerance(), updated.getDateWindowMinutes()),
                "USER");

        return updated;
    }

    private void clearPreviousDefaults() {
        toleranceConfigRepository.findAll().forEach(cfg -> {
            if (cfg.isDefault()) {
                cfg.setDefault(false);
                toleranceConfigRepository.save(cfg);
            }
        });
    }
}
