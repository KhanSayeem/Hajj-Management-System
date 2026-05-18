package com.hupms.controller;

import com.hupms.dto.response.AuditLogResponse;
import com.hupms.service.AuditService;
import com.hupms.util.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AuditLogResponse>> list(@RequestParam(required = false) String entityType,
                                                    @RequestParam(required = false) Long entityId) {
        return ApiResponse.success("Audit logs retrieved", auditService.list(entityType, entityId));
    }
}
