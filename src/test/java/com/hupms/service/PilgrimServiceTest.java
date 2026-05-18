package com.hupms.service;

import com.hupms.enums.Gender;
import com.hupms.enums.PilgrimStatus;
import com.hupms.exception.GroupCapacityExceededException;
import com.hupms.exception.UnauthorizedAccessException;
import com.hupms.model.Group;
import com.hupms.model.Pilgrim;
import com.hupms.repository.GroupRepository;
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
import static org.mockito.Mockito.when;

class PilgrimServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PilgrimRepository pilgrimRepository = mock(PilgrimRepository.class);
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final PilgrimService service = new PilgrimService(
            userRepository,
            pilgrimRepository,
            groupRepository,
            auditService,
            passwordEncoder
    );

    @Test
    void assignGroupRejectsFullGroup() {
        Group group = group(10L, 99L, 2);
        Pilgrim pilgrim = pilgrim(20L, null, PilgrimStatus.REGISTERED);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(pilgrimRepository.countByGroupId(10L)).thenReturn(2);

        assertThrows(GroupCapacityExceededException.class,
                () -> service.assignGroup(20L, 10L, 99L));
    }

    @Test
    void assignGroupRejectsForeignPilgrim() {
        Group targetGroup = group(10L, 99L, 2);
        Pilgrim pilgrim = pilgrim(20L, 11L, PilgrimStatus.REGISTERED);
        when(pilgrimRepository.findById(20L)).thenReturn(Optional.of(pilgrim));
        when(groupRepository.existsByIdAndAgentId(11L, 99L)).thenReturn(false);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(targetGroup));

        assertThrows(UnauthorizedAccessException.class,
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
