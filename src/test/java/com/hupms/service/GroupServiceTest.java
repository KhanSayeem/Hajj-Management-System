package com.hupms.service;

import com.hupms.dto.request.GroupRequest;
import com.hupms.enums.Role;
import com.hupms.exception.GroupCapacityExceededException;
import com.hupms.exception.UnauthorizedAccessException;
import com.hupms.model.Group;
import com.hupms.model.TravelPackage;
import com.hupms.model.User;
import com.hupms.repository.GroupRepository;
import com.hupms.repository.PackageRepository;
import com.hupms.repository.PilgrimRepository;
import com.hupms.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupServiceTest {
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final PackageRepository packageRepository = mock(PackageRepository.class);
    private final PilgrimRepository pilgrimRepository = mock(PilgrimRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final GroupService service = new GroupService(
            groupRepository,
            packageRepository,
            pilgrimRepository,
            userRepository,
            auditService
    );

    @Test
    void adminCreateRequiresAgentAssignee() {
        GroupRequest request = new GroupRequest("Dhaka A", 30L, 20, null);
        when(packageRepository.findById(30L)).thenReturn(Optional.of(new TravelPackage()));

        assertThrows(IllegalArgumentException.class, () -> service.create(request, 1L, Role.ADMIN));
    }

    @Test
    void agentCannotCreateGroupForAnotherAgent() {
        GroupRequest request = new GroupRequest("Dhaka A", 30L, 20, 99L);
        when(packageRepository.findById(30L)).thenReturn(Optional.of(new TravelPackage()));

        assertThrows(UnauthorizedAccessException.class, () -> service.create(request, 2L, Role.AGENT));
    }

    @Test
    void groupUpdateRejectsMaxSizeBelowCurrentOccupancy() {
        Group group = new Group();
        group.setId(10L);
        group.setAgentId(99L);
        group.setPackageId(30L);
        group.setMaxSize(10);
        GroupRequest request = new GroupRequest("Dhaka A", 30L, 2, null);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(pilgrimRepository.countByGroupId(10L)).thenReturn(3);

        assertThrows(GroupCapacityExceededException.class, () -> service.update(10L, request, 99L, Role.AGENT));
    }

    @Test
    void adminCreateRejectsNonAgentAssignee() {
        GroupRequest request = new GroupRequest("Dhaka A", 30L, 20, 2L);
        User user = new User();
        user.setRole(Role.PILGRIM);
        when(packageRepository.findById(30L)).thenReturn(Optional.of(new TravelPackage()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> service.create(request, 1L, Role.ADMIN));
    }
}
