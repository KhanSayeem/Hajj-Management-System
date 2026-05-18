package com.hupms.service;

import com.hupms.dto.request.PackageRequest;
import com.hupms.dto.response.PackageResponse;
import com.hupms.exception.ResourceNotFoundException;
import com.hupms.model.TravelPackage;
import com.hupms.repository.GroupRepository;
import com.hupms.repository.PackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageService {
    private final PackageRepository packageRepository;
    private final GroupRepository groupRepository;

    public PackageService(PackageRepository packageRepository, GroupRepository groupRepository) {
        this.packageRepository = packageRepository;
        this.groupRepository = groupRepository;
    }

    public PackageResponse create(PackageRequest request, Long actorId) {
        TravelPackage value = toPackage(request);
        value.setCreatedBy(actorId);
        value.setId(packageRepository.save(value));
        return PackageResponse.from(value);
    }

    public List<PackageResponse> list() {
        return packageRepository.findAll().stream().map(PackageResponse::from).toList();
    }

    public PackageResponse get(Long id) {
        return PackageResponse.from(find(id));
    }

    public PackageResponse update(Long id, PackageRequest request) {
        TravelPackage value = find(id);
        value.setName(request.name());
        value.setType(request.type());
        value.setYear(request.year());
        value.setCapacity(request.capacity());
        value.setPriceUsd(request.priceUsd());
        value.setDepartureDate(request.departureDate());
        value.setReturnDate(request.returnDate());
        packageRepository.update(value);
        return PackageResponse.from(value);
    }

    public void delete(Long id) {
        find(id);
        if (groupRepository.countByPackageId(id) > 0) {
            throw new IllegalArgumentException("Package has attached groups");
        }
        packageRepository.delete(id);
    }

    private TravelPackage find(Long id) {
        return packageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Package not found"));
    }

    private TravelPackage toPackage(PackageRequest request) {
        TravelPackage value = new TravelPackage();
        value.setName(request.name());
        value.setType(request.type());
        value.setYear(request.year());
        value.setCapacity(request.capacity());
        value.setPriceUsd(request.priceUsd());
        value.setDepartureDate(request.departureDate());
        value.setReturnDate(request.returnDate());
        return value;
    }
}
