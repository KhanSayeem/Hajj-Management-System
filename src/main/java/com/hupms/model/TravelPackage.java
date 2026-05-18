package com.hupms.model;

import com.hupms.enums.PackageType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TravelPackage extends BaseEntity {
    private String name;
    private PackageType type;
    private Integer year;
    private Integer capacity;
    private BigDecimal priceUsd;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private Long createdBy;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PackageType getType() { return type; }
    public void setType(PackageType type) { this.type = type; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public BigDecimal getPriceUsd() { return priceUsd; }
    public void setPriceUsd(BigDecimal priceUsd) { this.priceUsd = priceUsd; }
    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
