package com.hupms.dto.request;

import com.hupms.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PilgrimRegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank String passportNumber,
        @Past LocalDate dateOfBirth,
        String nationality,
        String phone,
        @NotNull Gender gender,
        Long groupId,
        Long mahramId
) {}
