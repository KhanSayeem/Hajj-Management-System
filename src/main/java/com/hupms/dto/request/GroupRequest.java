package com.hupms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GroupRequest(@NotBlank String groupName, @NotNull Long packageId, @NotNull Integer maxSize,
                           Long agentId) {}
