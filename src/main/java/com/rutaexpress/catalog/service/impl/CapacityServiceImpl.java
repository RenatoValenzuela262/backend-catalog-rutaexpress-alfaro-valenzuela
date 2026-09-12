package com.rutaexpress.catalog.service.impl;

import com.rutaexpress.catalog.entities.ShippingServiceEntity;
import com.rutaexpress.catalog.exception.ConcurrentCapacityUpdateException;
import com.rutaexpress.catalog.exception.InsufficientCapacityException;
import com.rutaexpress.catalog.exception.ServiceNotFoundException;
import com.rutaexpress.catalog.repository.ShippingServiceRepository;
import com.rutaexpress.catalog.service.CapacityService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class CapacityServiceImpl implements CapacityService {

    private static final int MAX_RETRIES = 2;

    private final ShippingServiceRepository repository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void decrease(Long serviceId, Integer amount) {
        adjustCapacity(serviceId, amount, AdjustmentType.DECREASE);
    }

    @Override
    public void increase(Long serviceId, Integer amount) {
        adjustCapacity(serviceId, amount, AdjustmentType.INCREASE);
    }

    /**
     * Cada intento corre en una transacción propia (vía TransactionTemplate).
     * Si el flush falla por un conflicto de {@code @Version} (otra transacción
     * ajustó la capacidad mientras tanto), se reintenta una vez con datos
     * frescos; si el conflicto persiste se responde HTTP 409.
     */
    private void adjustCapacity(Long serviceId, Integer amount, AdjustmentType type) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                transactionTemplate.executeWithoutResult(status -> applyAdjustment(serviceId, amount, type));
                return;
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
                if (attempt == MAX_RETRIES) {
                    throw new ConcurrentCapacityUpdateException(serviceId);
                }
            }
        }
    }

    private void applyAdjustment(Long serviceId, Integer amount, AdjustmentType type) {
        ShippingServiceEntity entity = repository.findById(serviceId)
                .filter(ShippingServiceEntity::isActive)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        int total = entity.getTotalCapacity();
        int updated = switch (type) {
            case INCREASE -> Math.min(entity.getAvailableCapacity() + amount, total);
            case DECREASE -> entity.getAvailableCapacity() - amount;
        };

        if (updated < 0) {
            throw new InsufficientCapacityException(
                    "Capacidad insuficiente para el servicio " + entity.getCode());
        }

        entity.setAvailableCapacity(updated);
        repository.saveAndFlush(entity);
    }

    private enum AdjustmentType {
        DECREASE,
        INCREASE
    }
}