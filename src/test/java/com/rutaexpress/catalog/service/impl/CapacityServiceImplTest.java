package com.rutaexpress.catalog.service.impl;

import com.rutaexpress.catalog.entities.ShippingServiceEntity;
import com.rutaexpress.catalog.exception.ConcurrentCapacityUpdateException;
import com.rutaexpress.catalog.exception.InsufficientCapacityException;
import com.rutaexpress.catalog.exception.ServiceNotFoundException;
import com.rutaexpress.catalog.repository.ShippingServiceRepository;
import com.rutaexpress.catalog.service.CapacityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.OptimisticLockException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapacityServiceImplTest {

    @Mock
    private ShippingServiceRepository repository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private CapacityServiceImpl capacityService;

    private CapacityService service;

    @BeforeEach
    void setUp() {
        service = capacityService;
    }

    private ShippingServiceEntity serviceEntity(long id, int total, int available) {
        return ShippingServiceEntity.builder()
                .id(id).code("EXPRESS_2H").totalCapacity(total).availableCapacity(available).active(true).build();
    }

    private void runCallbackOnTemplate() {
        doAnswer(invocation -> {
            Consumer<?> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void decrease_success() {
        ShippingServiceEntity entity = serviceEntity(1L, 10, 7);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        runCallbackOnTemplate();

        service.decrease(1L, 3);

        verify(repository).saveAndFlush(entity);
        assertThat(entity.getAvailableCapacity()).isEqualTo(4);
    }

    @Test
    void decrease_insufficientCapacity_throws() {
        ShippingServiceEntity entity = serviceEntity(1L, 10, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        runCallbackOnTemplate();

        assertThatThrownBy(() -> service.decrease(1L, 5))
                .isInstanceOf(InsufficientCapacityException.class)
                .hasMessageContaining("EXPRESS_2H");

        verify(repository, never()).saveAndFlush(any(ShippingServiceEntity.class));
    }

    @Test
    void increase_success() {
        ShippingServiceEntity entity = serviceEntity(1L, 10, 4);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        runCallbackOnTemplate();

        service.increase(1L, 3);

        verify(repository).saveAndFlush(entity);
        assertThat(entity.getAvailableCapacity()).isEqualTo(7);
    }

    @Test
    void increase_doesNotExceedTotalCapacity() {
        ShippingServiceEntity entity = serviceEntity(1L, 10, 9);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        runCallbackOnTemplate();

        service.increase(1L, 5);

        verify(repository).saveAndFlush(entity);
        assertThat(entity.getAvailableCapacity()).isEqualTo(10);
    }

    @Test
    void decrease_serviceNotFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        runCallbackOnTemplate();

        assertThatThrownBy(() -> service.decrease(1L, 1))
                .isInstanceOf(ServiceNotFoundException.class);

        verify(repository, never()).saveAndFlush(any(ShippingServiceEntity.class));
    }

    @Test
    void decrease_inactiveService_throwsNotFound() {
        ShippingServiceEntity entity = ShippingServiceEntity.builder()
                .id(1L).code("EXPRESS_2H").active(false).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        runCallbackOnTemplate();

        assertThatThrownBy(() -> service.decrease(1L, 1))
                .isInstanceOf(ServiceNotFoundException.class);

        verify(repository, never()).saveAndFlush(any(ShippingServiceEntity.class));
    }

    @Test
    void adjust_retriesOnceAfterOptimisticLockConflict() {
        ShippingServiceEntity staleRead = serviceEntity(1L, 10, 7);
        ShippingServiceEntity freshRead = serviceEntity(1L, 10, 7);
        when(repository.findById(1L)).thenReturn(Optional.of(staleRead), Optional.of(freshRead));
        runCallbackOnTemplate();

        OptimisticLockException lockConflict = new OptimisticLockException("stale");
        doThrow(lockConflict)
                .doAnswer(invocation -> freshRead)
                .when(repository).saveAndFlush(any(ShippingServiceEntity.class));

        service.decrease(1L, 3);

        verify(repository, times(2)).findById(1L);
        verify(repository, times(2)).saveAndFlush(any(ShippingServiceEntity.class));
        assertThat(freshRead.getAvailableCapacity()).isEqualTo(4);
    }

    @Test
    void adjust_conflictPersistsAfterRetry_throwsConflict() {
        ShippingServiceEntity entity = serviceEntity(1L, 10, 7);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        runCallbackOnTemplate();

        OptimisticLockException lockConflict = new OptimisticLockException("stale");
        doThrow(lockConflict).when(repository).saveAndFlush(any(ShippingServiceEntity.class));

        assertThatThrownBy(() -> service.decrease(1L, 3))
                .isInstanceOf(ConcurrentCapacityUpdateException.class)
                .hasMessageContaining("Conflicto de concurrencia");

        verify(repository, times(2)).saveAndFlush(any(ShippingServiceEntity.class));
    }
}