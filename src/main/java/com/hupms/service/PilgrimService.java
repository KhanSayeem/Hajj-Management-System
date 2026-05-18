package com.hupms.service;

import com.hupms.dto.request.PilgrimRegisterRequest;
import com.hupms.dto.request.PilgrimUpdateRequest;
import com.hupms.dto.response.PilgrimResponse;
import com.hupms.enums.Gender;
import com.hupms.enums.PilgrimStatus;
import com.hupms.enums.Role;
import com.hupms.exception.DuplicatePassportException;
import com.hupms.exception.GroupCapacityExceededException;
import com.hupms.exception.ResourceNotFoundException;
import com.hupms.exception.UnauthorizedAccessException;
import com.hupms.model.Group;
import com.hupms.model.Pilgrim;
import com.hupms.model.User;
import com.hupms.repository.GroupRepository;
import com.hupms.repository.PilgrimRepository;
import com.hupms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PilgrimService implements AuditableService {
    private static final Map<PilgrimStatus, PilgrimStatus> NEXT_STATUS = Map.of(
            PilgrimStatus.REGISTERED, PilgrimStatus.APPROVED,
            PilgrimStatus.APPROVED, PilgrimStatus.DEPARTED,
            PilgrimStatus.DEPARTED, PilgrimStatus.IN_MAKKAH,
            PilgrimStatus.IN_MAKKAH, PilgrimStatus.RETURNED
    );

    private final UserRepository userRepository;
    private final PilgrimRepository pilgrimRepository;
    private final GroupRepository groupRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public PilgrimService(UserRepository userRepository, PilgrimRepository pilgrimRepository,
                          GroupRepository groupRepository, AuditService auditService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.pilgrimRepository = pilgrimRepository;
        this.groupRepository = groupRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PilgrimResponse register(PilgrimRegisterRequest request, Long agentId) {
        if (pilgrimRepository.existsByPassportNumber(request.passportNumber())) {
            throw new DuplicatePassportException("Passport number already exists");
        }
        if (request.groupId() != null) {
            ensureGroupOwnedAndAvailable(request.groupId(), agentId);
        }
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.passportNumber()));
        user.setRole(Role.PILGRIM);
        user.setActive(true);
        Long userId = userRepository.save(user);

        Pilgrim pilgrim = new Pilgrim();
        pilgrim.setUserId(userId);
        pilgrim.setGroupId(request.groupId());
        pilgrim.setPassportNumber(request.passportNumber());
        pilgrim.setDateOfBirth(request.dateOfBirth());
        pilgrim.setNationality(request.nationality());
        pilgrim.setPhone(request.phone());
        pilgrim.setGender(request.gender());
        pilgrim.setMahramId(request.mahramId());
        pilgrim.setStatus(PilgrimStatus.REGISTERED);
        validateMahram(pilgrim);
        pilgrim.setId(pilgrimRepository.save(pilgrim));
        log(agentId, "PILGRIM_REGISTERED", "Pilgrim", pilgrim.getId(), "{\"passport\":\"" + pilgrim.getPassportNumber() + "\"}");
        return PilgrimResponse.from(pilgrim);
    }

    public List<PilgrimResponse> listAll() {
        return pilgrimRepository.findAll().stream().map(PilgrimResponse::from).toList();
    }

    public List<PilgrimResponse> listMine(Long agentId) {
        return pilgrimRepository.findByAgentId(agentId).stream().map(PilgrimResponse::from).toList();
    }

    public PilgrimResponse getByIdForActor(Long pilgrimId, Long actorId, String roleName) {
        Role role = Role.valueOf(roleName);
        Pilgrim pilgrim = pilgrimRepository.findById(pilgrimId)
                .orElseThrow(() -> new ResourceNotFoundException("Pilgrim not found"));
        if (role == Role.ADMIN) {
            return PilgrimResponse.from(pilgrim);
        }
        if (role == Role.PILGRIM && actorId.equals(pilgrim.getUserId())) {
            return PilgrimResponse.from(pilgrim);
        }
        if (role == Role.AGENT && pilgrim.getGroupId() != null && groupRepository.existsByIdAndAgentId(pilgrim.getGroupId(), actorId)) {
            return PilgrimResponse.from(pilgrim);
        }
        throw new UnauthorizedAccessException("Cannot access this pilgrim");
    }

    public PilgrimResponse update(Long pilgrimId, PilgrimUpdateRequest request, Long agentId) {
        Pilgrim pilgrim = findOwnedPilgrim(pilgrimId, agentId);
        if (request.dateOfBirth() != null) pilgrim.setDateOfBirth(request.dateOfBirth());
        if (request.nationality() != null) pilgrim.setNationality(request.nationality());
        if (request.phone() != null) pilgrim.setPhone(request.phone());
        if (request.gender() != null) pilgrim.setGender(request.gender());
        pilgrim.setMahramId(request.mahramId());
        validateMahram(pilgrim);
        pilgrimRepository.update(pilgrim);
        return PilgrimResponse.from(pilgrim);
    }

    public PilgrimResponse updateStatus(Long pilgrimId, PilgrimStatus newStatus, Long actorId) {
        Pilgrim pilgrim = findOwnedPilgrim(pilgrimId, actorId);
        return updateStatusInternal(pilgrim, newStatus, actorId);
    }

    public PilgrimResponse updateStatus(Long pilgrimId, PilgrimStatus newStatus, Long actorId, Role role) {
        Pilgrim pilgrim = role == Role.ADMIN
                ? pilgrimRepository.findById(pilgrimId).orElseThrow(() -> new ResourceNotFoundException("Pilgrim not found"))
                : findOwnedPilgrim(pilgrimId, actorId);
        return updateStatusInternal(pilgrim, newStatus, actorId);
    }

    private PilgrimResponse updateStatusInternal(Pilgrim pilgrim, PilgrimStatus newStatus, Long actorId) {
        validateTransition(pilgrim.getStatus(), newStatus);
        pilgrimRepository.updateStatus(pilgrim.getId(), newStatus);
        log(actorId, "PILGRIM_STATUS_CHANGED", "Pilgrim", pilgrim.getId(),
                "{\"from\":\"" + pilgrim.getStatus() + "\",\"to\":\"" + newStatus + "\"}");
        pilgrim.setStatus(newStatus);
        return PilgrimResponse.from(pilgrim);
    }

    public PilgrimResponse assignGroup(Long pilgrimId, Long groupId, Long agentId) {
        Pilgrim pilgrim = pilgrimRepository.findById(pilgrimId)
                .orElseThrow(() -> new ResourceNotFoundException("Pilgrim not found"));
        if (pilgrim.getGroupId() != null && !groupRepository.existsByIdAndAgentId(pilgrim.getGroupId(), agentId)) {
            throw new UnauthorizedAccessException("Agent cannot access this pilgrim");
        }
        ensureGroupOwnedAndAvailable(groupId, agentId);
        pilgrimRepository.assignGroup(pilgrimId, groupId);
        pilgrim.setGroupId(groupId);
        log(agentId, "PILGRIM_GROUP_ASSIGNED", "Pilgrim", pilgrimId, "{\"groupId\":" + groupId + "}");
        return PilgrimResponse.from(pilgrim);
    }

    public void delete(Long pilgrimId) {
        pilgrimRepository.findById(pilgrimId).orElseThrow(() -> new ResourceNotFoundException("Pilgrim not found"));
        pilgrimRepository.delete(pilgrimId);
    }

    @Override
    public void log(Long actorId, String action, String entityType, Long entityId, String details) {
        auditService.log(actorId, action, entityType, entityId, details);
    }

    private Pilgrim findOwnedPilgrim(Long pilgrimId, Long agentId) {
        Pilgrim pilgrim = pilgrimRepository.findById(pilgrimId)
                .orElseThrow(() -> new ResourceNotFoundException("Pilgrim not found"));
        if (pilgrim.getGroupId() == null || !groupRepository.existsByIdAndAgentId(pilgrim.getGroupId(), agentId)) {
            throw new UnauthorizedAccessException("Agent cannot access this pilgrim");
        }
        return pilgrim;
    }

    private void ensureGroupOwnedAndAvailable(Long groupId, Long agentId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!group.getAgentId().equals(agentId)) {
            throw new UnauthorizedAccessException("Agent cannot access this group");
        }
        if (pilgrimRepository.countByGroupId(groupId) >= group.getMaxSize()) {
            throw new GroupCapacityExceededException("Group capacity exceeded");
        }
    }

    private void validateTransition(PilgrimStatus current, PilgrimStatus next) {
        if (next == PilgrimStatus.CANCELLED || next == current) {
            return;
        }
        if (NEXT_STATUS.get(current) != next) {
            throw new IllegalArgumentException("Invalid status transition from " + current + " to " + next);
        }
    }

    private void validateMahram(Pilgrim pilgrim) {
        if (pilgrim.getGender() == Gender.FEMALE && pilgrim.getMahramId() != null) {
            Pilgrim mahram = pilgrimRepository.findById(pilgrim.getMahramId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mahram pilgrim not found"));
            if (mahram.getGender() != Gender.MALE || !java.util.Objects.equals(mahram.getGroupId(), pilgrim.getGroupId())) {
                throw new IllegalArgumentException("Mahram must be a male pilgrim in the same group");
            }
        }
    }
}
