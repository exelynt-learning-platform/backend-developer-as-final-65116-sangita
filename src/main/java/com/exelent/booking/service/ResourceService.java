package com.exelent.booking.service;

import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.dto.PagedResponse;
import com.exelent.booking.dto.resource.ResourceRequest;
import com.exelent.booking.dto.resource.ResourceResponse;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ResourceResponse> findAll(Pageable pageable) {
        return PagedResponse.from(resourceRepository.findAll(pageable).map(ResourceResponse::from));
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
        try {
            resourceRepository.delete(resource);
            resourceRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, "Can't delete this resource, it already has reservations");
        }
    }

    public BookableResource getResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resource not found"));
    }
}
