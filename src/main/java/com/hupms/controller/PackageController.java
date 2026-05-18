package com.hupms.controller;

import com.hupms.dto.request.PackageRequest;
import com.hupms.dto.response.PackageResponse;
import com.hupms.model.User;
import com.hupms.service.AuthService;
import com.hupms.service.PackageService;
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
@RequestMapping("/api/packages")
public class PackageController {
    private final PackageService packageService;
    private final AuthService authService;

    public PackageController(PackageService packageService, AuthService authService) {
        this.packageService = packageService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PackageResponse> create(@Valid @RequestBody PackageRequest request, Principal principal) {
        User user = authService.currentUser(principal.getName());
        return ApiResponse.success("Package created successfully", packageService.create(request, user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ApiResponse<List<PackageResponse>> list() {
        return ApiResponse.success("Packages retrieved", packageService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ApiResponse<PackageResponse> get(@PathVariable Long id) {
        return ApiResponse.success("Package retrieved", packageService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PackageResponse> update(@PathVariable Long id, @Valid @RequestBody PackageRequest request) {
        return ApiResponse.success("Package updated successfully", packageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        packageService.delete(id);
        return ApiResponse.success("Package deleted successfully", null);
    }
}
