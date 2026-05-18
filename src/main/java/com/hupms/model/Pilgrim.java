package com.hupms.model;

import com.hupms.enums.Gender;
import com.hupms.enums.PilgrimStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pilgrim extends BaseEntity {
    private Long userId;
    private Long groupId;
    private String passportNumber;
    private LocalDate dateOfBirth;
    private String nationality;
    private String phone;
    private Gender gender;
    private Long mahramId;
    private PilgrimStatus status = PilgrimStatus.REGISTERED;
    private LocalDateTime updatedAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public Long getMahramId() { return mahramId; }
    public void setMahramId(Long mahramId) { this.mahramId = mahramId; }
    public PilgrimStatus getStatus() { return status; }
    public void setStatus(PilgrimStatus status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
