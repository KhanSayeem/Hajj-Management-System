package com.hupms.dto.request;

import com.hupms.enums.PilgrimStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull PilgrimStatus status) {}
