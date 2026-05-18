package com.hupms.service;

import com.hupms.enums.Gender;
import com.hupms.enums.PilgrimStatus;
import com.hupms.exception.GroupCapacityExceededException;
import com.hupms.exception.UnauthorizedAccessException;
import com.hupms.model.Group;
import com.hupms.model.Pilgrim;
import com.hupms.repository.GroupRepository;
import com.hupms.repository.PackageRepository;
import com.hupms.repository.PilgrimRepository;
import com.hupms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PilgrimServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PilgrimRepository pilgrimRepository = mock(PilgrimRepository.class);
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final PackageRepository packageRepository = mock(PackageRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final PilgrimService service = new PilgrimService(
            userRepository,
            pilgrimRepository,
            groupRepository,
            packageRepository,
            auditService,
            passwordEncoder
    );

    @Test
    void registerUsesProvidedPasswordForPilgrimAccount() {
        com.hupms.dto.request.PilgrimRegisterRequest request = new com.hupms.dto.request.PilgrimRegisterRequest(
                "Fatima Begum",
                "fatima@example.com",
                "securepass123",
                "BD1234567",
                LocalDate.of(1980, 5, 15),
                "Bangladeshi",
                "+8801711000000",
                Gender.FEMALE,
                null,
                null
        );
        when(passwordEncoder.encode("securepass123")).thenReturn("encoded");
        when(userRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(100L);
        when(pilgrimRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(20L);

        service.register(request, 99L);

        verify(passwordEncoder).encode("securepass123");
    }

    @Test
    void assignGroupRejectsFullGroup() {
        Group group = group(10L, 99L, 2);
        Pilgrim pilgrim = pilgrim(20L, null, PilgrimStatus.REGISTERED);
        when(groupRepository.lockById(10L)).thenReturn(Optional.of(group));
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(pilgrimRepository.countByGroupId(10L)).thenReturn(2);

        assertThrows(GroupCapacityExceededException.class,
                () -> service.assignGroup(20L, 10L, 99L));
    }

    @Test
    void assignGroupRejectsFullPackage() {
        Group group = group(10L, 99L, 5);
        Pilgrim pilgrim = pilgrim(20L, null, PilgrimStatus.REGISTERED);
        com.hupms.model.TravelPackage travelPackage = new com.hupms.model.TravelPackage();
        travelPackage.setId(30L);
        travelPackage.setCapacity(2);
        when(groupRepository.lockById(10L)).thenReturn(Optional.of(group));
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(pilgrimRepository.countByGroupId(10L)).thenReturn(1);
        when(packageRepository.lockById(30L)).thenReturn(Optional.of(travelPackage));
        when(pilgrimRepository.countByPackageIdExcluding(30L, 20L)).thenReturn(2);

        assertThrows(GroupCapacityExceededException.class,
                () -> service.assignGroup(20L, 10L, 99L));
    }

    @Test
    void assignGroupAllowsReassignmentWithinFullPackageWhenExcludingMovedPilgrim() {
        Group group = group(10L, 99L, 5);
        Pilgrim pilgrim = pilgrim(20L, 11L, PilgrimStatus.REGISTERED);
        com.hupms.model.TravelPackage travelPackage = new com.hupms.model.TravelPackage();
        travelPackage.setId(30L);
        travelPackage.setCapacity(2);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(11L, 99L)).thenReturn(true);
        when(groupRepository.lockById(10L)).thenReturn(Optional.of(group));
        when(pilgrimRepository.countByGroupId(10L)).thenReturn(1);
        when(packageRepository.lockById(30L)).thenReturn(Optional.of(travelPackage));
        when(pilgrimRepository.countByPackageIdExcluding(30L, 20L)).thenReturn(1);

        assertDoesNotThrow(() -> service.assignGroup(20L, 10L, 99L));
    }

    @Test
    void assignGroupRejectsForeignPilgrim() {
        Group targetGroup = group(10L, 99L, 2);
        Pilgrim pilgrim = pilgrim(20L, 11L, PilgrimStatus.REGISTERED);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(11L, 99L)).thenReturn(false);
        when(groupRepository.lockById(10L)).thenReturn(Optional.of(targetGroup));

        assertThrows(UnauthorizedAccessException.class,
                () -> service.assignGroup(20L, 10L, 99L));
    }

    @Test
    void assignGroupRejectsMovingFemaleAwayFromMahram() {
        Group targetGroup = group(10L, 99L, 2);
        Pilgrim pilgrim = pilgrim(20L, 11L, PilgrimStatus.REGISTERED);
        pilgrim.setGender(Gender.FEMALE);
        pilgrim.setMahramId(21L);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(11L, 99L)).thenReturn(true);
        when(groupRepository.lockById(10L)).thenReturn(Optional.of(targetGroup));
        when(pilgrimRepository.countByGroupId(10L)).thenReturn(0);
        com.hupms.model.TravelPackage travelPackage = new com.hupms.model.TravelPackage();
        travelPackage.setId(30L);
        travelPackage.setCapacity(2);
        when(packageRepository.lockById(30L)).thenReturn(Optional.of(travelPackage));
        when(pilgrimRepository.findById(21L)).thenReturn(Optional.of(pilgrim(21L, 11L, PilgrimStatus.REGISTERED)));

        assertThrows(IllegalArgumentException.class,
                () -> service.assignGroup(20L, 10L, 99L));
    }

    @Test
    void assignGroupRejectsMovingMaleWithMahramDependents() {
        Group targetGroup = group(10L, 99L, 2);
        Pilgrim pilgrim = pilgrim(20L, 11L, PilgrimStatus.REGISTERED);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(11L, 99L)).thenReturn(true);
        when(groupRepository.lockById(10L)).thenReturn(Optional.of(targetGroup));
        when(pilgrimRepository.countByGroupId(10L)).thenReturn(0);
        when(pilgrimRepository.existsByMahramId(20L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.assignGroup(20L, 10L, 99L));
    }

    @Test
    void updateStatusAllowsCancellationFromAnyState() {
        Pilgrim pilgrim = pilgrim(20L, 10L, PilgrimStatus.IN_MAKKAH);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(10L, 99L)).thenReturn(true);

        assertDoesNotThrow(() -> service.updateStatus(20L, PilgrimStatus.CANCELLED, 99L));
    }

    @Test
    void updateStatusRejectsSkippedTransition() {
        Pilgrim pilgrim = pilgrim(20L, 10L, PilgrimStatus.REGISTERED);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(10L, 99L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(20L, PilgrimStatus.DEPARTED, 99L));
    }

    @Test
    void listForAgentRejectsForeignPilgrimAccess() {
        Pilgrim pilgrim = pilgrim(20L, 10L, PilgrimStatus.REGISTERED);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(10L, 99L)).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class,
                () -> service.getByIdForActor(20L, 99L, "AGENT"));
    }

    private Group group(Long id, Long agentId, int maxSize) {
        Group group = new Group();
        group.setId(id);
        group.setAgentId(agentId);
        group.setPackageId(30L);
        group.setMaxSize(maxSize);
        return group;
    }

    private Pilgrim pilgrim(Long id, Long groupId, PilgrimStatus status) {
        Pilgrim pilgrim = new Pilgrim();
        pilgrim.setId(id);
        pilgrim.setGroupId(groupId);
        pilgrim.setPassportNumber("BD" + id);
        pilgrim.setDateOfBirth(LocalDate.of(1980, 1, 1));
        pilgrim.setNationality("Bangladeshi");
        pilgrim.setPhone("+8801711000000");
        pilgrim.setGender(Gender.MALE);
        pilgrim.setStatus(status);
        return pilgrim;
    }
}
