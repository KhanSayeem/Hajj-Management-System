package com.hupms.controller;

import com.hupms.dto.request.AssignGroupRequest;
import com.hupms.dto.request.PilgrimRegisterRequest;
import com.hupms.dto.request.PilgrimUpdateRequest;
import com.hupms.dto.request.StatusUpdateRequest;
import com.hupms.dto.response.PilgrimResponse;
import com.hupms.model.User;
import com.hupms.service.AuthService;
import com.hupms.service.PilgrimService;
import com.hupms.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/pilgrims")
public class PilgrimController {
    private final PilgrimService pilgrimService;
    private final AuthService authService;

    public PilgrimController(PilgrimService pilgrimService, AuthService authService) {
        this.pilgrimService = pilgrimService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<PilgrimResponse> register(@Valid @RequestBody PilgrimRegisterRequest request, Principal principal) {
        User user = authService.currentUser(principal.getName());
        return ApiResponse.success("Pilgrim registered successfully", pilgrimService.register(request, user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PilgrimResponse>> listAll() {
        return ApiResponse.success("Pilgrims retrieved", pilgrimService.listAll());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<List<PilgrimResponse>> listMine(Principal principal) {
        return ApiResponse.success("Pilgrims retrieved", pilgrimService.listMine(authService.currentUser(principal.getName()).getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','PILGRIM')")
    public ApiResponse<PilgrimResponse> get(@PathVariable Long id, Principal principal) {
        User user = authService.currentUser(principal.getName());
        return ApiResponse.success("Pilgrim retrieved", pilgrimService.getByIdForActor(id, user.getId(), user.getRole().name()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<PilgrimResponse> update(@PathVariable Long id, @Valid @RequestBody PilgrimUpdateRequest request,
                                               Principal principal) {
        return ApiResponse.success("Pilgrim updated successfully",
                pilgrimService.update(id, request, authService.currentUser(principal.getName()).getId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ApiResponse<PilgrimResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request,
                                                     Principal principal) {
        User user = authService.currentUser(principal.getName());
        return ApiResponse.success("Pilgrim status updated successfully",
                pilgrimService.updateStatus(id, request.status(), user.getId(), user.getRole()));
    }

    @PatchMapping("/{id}/assign-group")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<PilgrimResponse> assignGroup(@PathVariable Long id, @Valid @RequestBody AssignGroupRequest request,
                                                    Principal principal) {
        return ApiResponse.success("Pilgrim assigned to group successfully",
                pilgrimService.assignGroup(id, request.groupId(), authService.currentUser(principal.getName()).getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pilgrimService.delete(id);
        return ApiResponse.success("Pilgrim deleted successfully", null);
    }
}
