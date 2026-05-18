package com.hupms.dto.response;

import com.hupms.enums.PackageType;
import com.hupms.model.TravelPackage;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PackageResponse(Long id, String name, PackageType type, Integer year, Integer capacity,
                              BigDecimal priceUsd, LocalDate departureDate, LocalDate returnDate, Long createdBy) {
    public static PackageResponse from(TravelPackage value) {
        return new PackageResponse(value.getId(), value.getName(), value.getType(), value.getYear(),
                value.getCapacity(), value.getPriceUsd(), value.getDepartureDate(), value.getReturnDate(),
                value.getCreatedBy());
    }
}
