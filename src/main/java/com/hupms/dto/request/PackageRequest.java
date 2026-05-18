package com.hupms.dto.request;

import com.hupms.enums.PackageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PackageRequest(
        @NotBlank String name,
        @NotNull PackageType type,
        @NotNull Integer year,
        @NotNull Integer capacity,
        @NotNull BigDecimal priceUsd,
        LocalDate departureDate,
        LocalDate returnDate
) {}
