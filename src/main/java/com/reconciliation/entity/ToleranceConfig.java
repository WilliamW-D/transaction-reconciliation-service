package com.reconciliation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tolerance_configs")
public class ToleranceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "amount_tolerance", nullable = false, precision = 19, scale = 4)
    private BigDecimal amountTolerance;

    @Column(name = "fee_tolerance", nullable = false, precision = 19, scale = 4)
    private BigDecimal feeTolerance;

    @Column(name = "date_window_minutes", nullable = false)
    private int dateWindowMinutes;

    @Column(name = "currency_strict", nullable = false)
    private boolean currencyStrict = true;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ToleranceConfig() {}

    public ToleranceConfig(String name, BigDecimal amountTolerance, BigDecimal feeTolerance, int dateWindowMinutes, boolean currencyStrict, boolean isDefault) {
        this.name = name;
        this.amountTolerance = amountTolerance;
        this.feeTolerance = feeTolerance;
        this.dateWindowMinutes = dateWindowMinutes;
        this.currencyStrict = currencyStrict;
        this.isDefault = isDefault;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmountTolerance() { return amountTolerance; }
    public void setAmountTolerance(BigDecimal amountTolerance) { this.amountTolerance = amountTolerance; }

    public BigDecimal getFeeTolerance() { return feeTolerance; }
    public void setFeeTolerance(BigDecimal feeTolerance) { this.feeTolerance = feeTolerance; }

    public int getDateWindowMinutes() { return dateWindowMinutes; }
    public void setDateWindowMinutes(int dateWindowMinutes) { this.dateWindowMinutes = dateWindowMinutes; }

    public boolean isCurrencyStrict() { return currencyStrict; }
    public void setCurrencyStrict(boolean currencyStrict) { this.currencyStrict = currencyStrict; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean defaultConfig) { isDefault = defaultConfig; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
