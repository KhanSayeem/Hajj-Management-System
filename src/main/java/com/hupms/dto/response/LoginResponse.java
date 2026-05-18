package com.hupms.dto.response;

public record LoginResponse(String token, String tokenType, long expiresIn, UserResponse user) {}
