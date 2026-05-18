package com.hupms.dto.response;

import com.hupms.enums.Role;
import com.hupms.model.User;

public record UserResponse(Long id, String fullName, String email, Role role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
