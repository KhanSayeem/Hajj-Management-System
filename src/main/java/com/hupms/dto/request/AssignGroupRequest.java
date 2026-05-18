package com.hupms.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignGroupRequest(@NotNull Long groupId) {}
