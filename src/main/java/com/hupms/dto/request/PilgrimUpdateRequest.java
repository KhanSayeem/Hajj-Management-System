package com.hupms.dto.request;

import com.hupms.enums.Gender;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PilgrimUpdateRequest(
        @Past LocalDate dateOfBirth,
        String nationality,
        String phone,
        Gender gender,
        Long mahramId
) {}
