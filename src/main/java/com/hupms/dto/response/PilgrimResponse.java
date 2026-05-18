package com.hupms.dto.response;

import com.hupms.enums.Gender;
import com.hupms.enums.PilgrimStatus;
import com.hupms.model.Pilgrim;

import java.time.LocalDate;

public record PilgrimResponse(Long id, Long userId, Long groupId, String passportNumber, LocalDate dateOfBirth,
                              String nationality, String phone, Gender gender, Long mahramId,
                              PilgrimStatus status) {
    public static PilgrimResponse from(Pilgrim pilgrim) {
        return new PilgrimResponse(pilgrim.getId(), pilgrim.getUserId(), pilgrim.getGroupId(),
                pilgrim.getPassportNumber(), pilgrim.getDateOfBirth(), pilgrim.getNationality(),
                pilgrim.getPhone(), pilgrim.getGender(), pilgrim.getMahramId(), pilgrim.getStatus());
    }
}
