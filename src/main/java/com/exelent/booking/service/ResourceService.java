package com.exelent.booking.service;

import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.dto.PagedResponse;
import com.exelent.booking.dto.resource.ResourceFilterRequest;
import com.exelent.booking.dto.resource.ResourceRequest;
import com.exelent.booking.dto.resource.ResourceResponse;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.exception.ApiMessages;
import com.exelent.booking.repository.ReservationRepository;
import com.exelent.booking.repository.ResourceRepository;
import com.exelent.booking.repository.ResourceSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ResourceResponse> findAll(ResourceFilterRequest filter, Pageable pageable) {
        ResourceFilterRequest safeFilter = filter == null ? new ResourceFilterRequest(null, null, null) : filter;
        return PagedResponse.from(
                resourceRepository
                        .findAll(ResourceSpecifications.withFilters(
                                safeFilter.type(), safeFilter.available(), safeFilter.name()), pageable)
                        .map(ResourceResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public ResourceResponse findById(Long id) {
        return ResourceResponse.from(getResource(id));
    }

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        BookableResource resource = BookableResource.builder()
                .name(request.name().trim())
                .description(request.description())
                .type(request.type())
                .location(request.location())
                .hourlyRate(request.hourlyRate())
                .available(request.available() == null || request.available())
                .build();
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponse update(Long id, ResourceRequest request) {
        BookableResource resource = getResource(id);
        resource.setName(request.name().trim());
        resource.setDescription(request.description());
        resource.setType(request.type());
        resource.setLocation(request.location());
        resource.setHourlyRate(request.hourlyRate());
        resource.setAvailable(request.available() == null || request.available());
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public void delete(Long id) {
        BookableResource resource = getResource(id);
        if (reservationRepository.existsByResource_Id(id)) {
            throw new ApiException(HttpStatus.CONFLICT, ApiMessages.RESOURCE_HAS_RESERVATIONS);
        }
        resourceRepository.delete(resource);
    }

    public BookableResource getResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiMessages.RESOURCE_NOT_FOUND));
    }

    public BookableResource lockResource(Long id) {
        return resourceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiMessages.RESOURCE_NOT_FOUND));
    }
}
