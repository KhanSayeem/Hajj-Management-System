package com.hupms.dto.response;

import com.hupms.model.Group;

import java.util.List;

public record GroupResponse(Long id, String groupName, Long packageId, Long agentId, Integer maxSize,
                            List<PilgrimResponse> pilgrims) {
    public static GroupResponse from(Group group, List<PilgrimResponse> pilgrims) {
        return new GroupResponse(group.getId(), group.getGroupName(), group.getPackageId(),
                group.getAgentId(), group.getMaxSize(), pilgrims);
    }
}
