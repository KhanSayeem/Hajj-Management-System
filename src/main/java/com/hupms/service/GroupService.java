package com.hupms.service;

import com.hupms.dto.request.GroupRequest;
import com.hupms.dto.response.GroupResponse;
import com.hupms.dto.response.PilgrimResponse;
import com.hupms.enums.Role;
import com.hupms.exception.ResourceNotFoundException;
import com.hupms.exception.UnauthorizedAccessException;
import com.hupms.model.Group;
import com.hupms.repository.GroupRepository;
import com.hupms.repository.PackageRepository;
import com.hupms.repository.PilgrimRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService implements AuditableService {
    private final GroupRepository groupRepository;
    private final PackageRepository packageRepository;
    private final PilgrimRepository pilgrimRepository;
    private final AuditService auditService;

    public GroupService(GroupRepository groupRepository, PackageRepository packageRepository,
                        PilgrimRepository pilgrimRepository, AuditService auditService) {
        this.groupRepository = groupRepository;
        this.packageRepository = packageRepository;
        this.pilgrimRepository = pilgrimRepository;
        this.auditService = auditService;
    }

    public GroupResponse create(GroupRequest request, Long agentId) {
        packageRepository.findById(request.packageId()).orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        Group group = new Group();
        group.setGroupName(request.groupName());
        group.setPackageId(request.packageId());
        group.setAgentId(agentId);
        group.setMaxSize(request.maxSize());
        group.setId(groupRepository.save(group));
        log(agentId, "GROUP_CREATED", "Group", group.getId(), "{\"name\":\"" + group.getGroupName() + "\"}");
        return withPilgrims(group);
    }

    public List<GroupResponse> listAll() {
        return groupRepository.findAll().stream().map(this::withPilgrims).toList();
    }

    public List<GroupResponse> listMine(Long agentId) {
        return groupRepository.findByAgentId(agentId).stream().map(this::withPilgrims).toList();
    }

    public GroupResponse get(Long id, Long actorId, Role role) {
        Group group = find(id);
        authorizeOwnerOrAdmin(group, actorId, role);
        return withPilgrims(group);
    }

    public GroupResponse update(Long id, GroupRequest request, Long actorId, Role role) {
        Group group = find(id);
        authorizeOwnerOrAdmin(group, actorId, role);
        group.setGroupName(request.groupName());
        group.setMaxSize(request.maxSize());
        groupRepository.update(group);
        return withPilgrims(group);
    }

    public void delete(Long id) {
        find(id);
        if (pilgrimRepository.countByGroupId(id) > 0) {
            throw new IllegalArgumentException("Group has assigned pilgrims");
        }
        groupRepository.delete(id);
    }

    @Override
    public void log(Long actorId, String action, String entityType, Long entityId, String details) {
        auditService.log(actorId, action, entityType, entityId, details);
    }

    private Group find(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private void authorizeOwnerOrAdmin(Group group, Long actorId, Role role) {
        if (role != Role.ADMIN && !group.getAgentId().equals(actorId)) {
            throw new UnauthorizedAccessException("Agent cannot access this group");
        }
    }

    private GroupResponse withPilgrims(Group group) {
        List<PilgrimResponse> pilgrims = pilgrimRepository.findByGroupId(group.getId()).stream()
                .map(PilgrimResponse::from)
                .toList();
        return GroupResponse.from(group, pilgrims);
    }
}
