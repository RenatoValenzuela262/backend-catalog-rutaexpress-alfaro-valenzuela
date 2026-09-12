package com.rutaexpress.catalog.service.impl;

import com.rutaexpress.catalog.dto.request.CreateServiceRequest;
import com.rutaexpress.catalog.dto.request.UpdateServiceRequest;
import com.rutaexpress.catalog.dto.response.PageResponse;
import com.rutaexpress.catalog.dto.response.ServiceResponse;
import com.rutaexpress.catalog.entities.ShippingServiceEntity;
import com.rutaexpress.catalog.exception.DuplicateServiceCodeException;
import com.rutaexpress.catalog.exception.InsufficientCapacityException;
import com.rutaexpress.catalog.exception.ServiceNotFoundException;
import com.rutaexpress.catalog.mapper.ServiceMapper;
import com.rutaexpress.catalog.repository.ShippingServiceRepository;
import com.rutaexpress.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ShippingServiceRepository repository;
    private final ServiceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> listServices(Boolean active, int page, int size) {
        boolean activeFilter = active == null || active;
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size));
        Page<ServiceResponse> mapped = repository.findByActive(activeFilter, pageable)
                .map(mapper::toResponse);
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getService(Long id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id)));
    }

    @Override
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request) {
        String code = request.getCode().trim();
        if (repository.existsByCode(code)) {
            throw new DuplicateServiceCodeException(code);
        }
        ShippingServiceEntity entity = mapper.toEntity(request);
        entity.setCode(code);
        entity.setAvailableCapacity(entity.getTotalCapacity());
        entity.setActive(true);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public ServiceResponse updateService(Long id, UpdateServiceRequest request) {
        ShippingServiceEntity entity = findActive(id);

        int newTotal = request.getTotalCapacity();
        int capacityDelta = newTotal - entity.getTotalCapacity();
        int newAvailable = entity.getAvailableCapacity() + capacityDelta;
        if (newAvailable < 0) {
            throw new InsufficientCapacityException(
                    "No se puede reducir la capacidad total del servicio " + entity.getCode()
                            + ": quedaría por debajo de la capacidad ya comprometida");
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setTariff(request.getTariff());
        entity.setTotalCapacity(newTotal);
        entity.setAvailableCapacity(newAvailable);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        ShippingServiceEntity entity = findActive(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private ShippingServiceEntity findActive(Long id) {
        return repository.findById(id)
                .filter(ShippingServiceEntity::isActive)
                .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}