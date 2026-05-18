package com.hupms.controller;

import com.hupms.dto.request.GroupRequest;
import com.hupms.dto.response.GroupResponse;
import com.hupms.model.User;
import com.hupms.service.AuthService;
import com.hupms.service.GroupService;
import com.hupms.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;
    private final AuthService authService;

    public GroupController(GroupService groupService, AuthService authService) {
        this.groupService = groupService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ApiResponse<GroupResponse> create(@Valid @RequestBody GroupRequest request, Principal principal) {
        User user = authService.currentUser(principal.getName());
        return ApiResponse.success("Group created successfully", groupService.create(request, user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<GroupResponse>> listAll() {
        return ApiResponse.success("Groups retrieved", groupService.listAll());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('AGENT')")
    public ApiResponse<List<GroupResponse>> listMine(Principal principal) {
        return ApiResponse.success("Groups retrieved", groupService.listMine(authService.currentUser(principal.getName()).getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ApiResponse<GroupResponse> get(@PathVariable Long id, Principal principal) {
        User user = authService.currentUser(principal.getName());
        return ApiResponse.success("Group retrieved", groupService.get(id, user.getId(), user.getRole()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ApiResponse<GroupResponse> update(@PathVariable Long id, @Valid @RequestBody GroupRequest request, Principal principal) {
        User user = authService.currentUser(principal.getName());
        return ApiResponse.success("Group updated successfully", groupService.update(id, request, user.getId(), user.getRole()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        groupService.delete(id);
        return ApiResponse.success("Group deleted successfully", null);
    }
}
