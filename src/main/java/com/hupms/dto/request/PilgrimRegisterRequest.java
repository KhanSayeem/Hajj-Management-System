package com.hupms.dto.request;

import com.hupms.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PilgrimRegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @Size(min = 8) String password,
        @NotBlank String passportNumber,
        @Past LocalDate dateOfBirth,
        String nationality,
        String phone,
        @NotNull Gender gender,
        Long groupId,
        Long mahramId
) {}
