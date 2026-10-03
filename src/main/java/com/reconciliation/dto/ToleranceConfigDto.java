package com.reconciliation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ToleranceConfigDto {
    private Long id;

    @NotBlank(message = "Config name is required")
    private String name;

    @DecimalMin(value = "0.0", message = "Amount tolerance must be >= 0.0")
    private BigDecimal amountTolerance;

    @DecimalMin(value = "0.0", message = "Fee tolerance must be >= 0.0")
    private BigDecimal feeTolerance;

    @Min(value = 0, message = "Date window in minutes must be >= 0")
    private int dateWindowMinutes;

    private boolean currencyStrict;
    private boolean isDefault;

    public ToleranceConfigDto() {}

    public ToleranceConfigDto(Long id, String name, BigDecimal amountTolerance, BigDecimal feeTolerance, int dateWindowMinutes, boolean currencyStrict, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.amountTolerance = amountTolerance;
        this.feeTolerance = feeTolerance;
        this.dateWindowMinutes = dateWindowMinutes;
        this.currencyStrict = currencyStrict;
        this.isDefault = isDefault;
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
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
