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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private ShippingServiceRepository repository;

    @Mock
    private ServiceMapper mapper;

    @InjectMocks
    private CatalogServiceImpl service;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = service;
    }

    @Test
    void createService_success_setsActiveAndAvailableEqualTotal() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setCode(" EXPRESS_2H ");
        request.setName("Express 2 horas");
        request.setTariff(new BigDecimal("2500.00"));
        request.setTotalCapacity(100);

        ShippingServiceEntity entity = ShippingServiceEntity.builder()
                .code(request.getCode())
                .totalCapacity(100)
                .build();

        when(repository.existsByCode("EXPRESS_2H")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(ServiceResponse.builder().id(1L).code("EXPRESS_2H").build());
        when(repository.save(entity)).thenAnswer(inv -> {
            entity.setId(1L);
            return entity;
        });

        ServiceResponse response = catalogService.createService(request);

        assertThat(response).isNotNull();
        ArgumentCaptor<ShippingServiceEntity> captor = ArgumentCaptor.forClass(ShippingServiceEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("EXPRESS_2H");
        assertThat(captor.getValue().getAvailableCapacity()).isEqualTo(100);
        assertThat(captor.getValue().isActive()).isTrue();
    }

    @Test
    void createService_duplicateCode_throws() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setCode("EXPRESS_2H");
        request.setName("Express 2 horas");
        request.setTariff(new BigDecimal("2500.00"));
        request.setTotalCapacity(100);

        when(repository.existsByCode("EXPRESS_2H")).thenReturn(true);

        assertThatThrownBy(() -> catalogService.createService(request))
                .isInstanceOf(DuplicateServiceCodeException.class)
                .hasMessageContaining("EXPRESS_2H");

        verify(repository, never()).save(any(ShippingServiceEntity.class));
    }

    @Test
    void getService_success() {
        ShippingServiceEntity entity = ShippingServiceEntity.builder()
                .id(1L).code("EXPRESS_2H").name("Express").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        ServiceResponse expected = ServiceResponse.builder().id(1L).code("EXPRESS_2H").build();
        when(mapper.toResponse(entity)).thenReturn(expected);

        ServiceResponse result = catalogService.getService(1L);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getService_notFound_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getService(99L))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void listServices_defaultsToActive() {
        when(repository.findByActive(eq(true), any(Pageable.class))).thenReturn(Page.empty());

        PageResponse<ServiceResponse> result = catalogService.listServices(null, 0, 20);

        assertThat(result.getTotalElements()).isZero();
        verify(repository).findByActive(eq(true), any(Pageable.class));
    }

    @Test
    void listServices_explicitInactive() {
        when(repository.findByActive(eq(false), any(Pageable.class))).thenReturn(Page.empty());

        catalogService.listServices(false, 0, 20);

        verify(repository).findByActive(eq(false), any(Pageable.class));
    }

    @Test
    void updateService_totalCapacityIncrease_increasesAvailable() {
        ShippingServiceEntity entity = ShippingServiceEntity.builder()
                .id(1L).code("EXPRESS_2H").totalCapacity(100).availableCapacity(50).active(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Express 2 horas");
        request.setTariff(new BigDecimal("2800.00"));
        request.setTotalCapacity(150);

        catalogService.updateService(1L, request);

        ArgumentCaptor<ShippingServiceEntity> captor = ArgumentCaptor.forClass(ShippingServiceEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTotalCapacity()).isEqualTo(150);
        assertThat(captor.getValue().getAvailableCapacity()).isEqualTo(100);
    }

    @Test
    void updateService_totalCapacityDecreaseBelowCommitted_throws() {
        ShippingServiceEntity entity = ShippingServiceEntity.builder()
                .id(1L).code("EXPRESS_2H").totalCapacity(100).availableCapacity(50).active(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Express 2 horas");
        request.setTariff(new BigDecimal("2800.00"));
        request.setTotalCapacity(40);

        assertThatThrownBy(() -> catalogService.updateService(1L, request))
                .isInstanceOf(InsufficientCapacityException.class);

        verify(repository, never()).save(any(ShippingServiceEntity.class));
    }

    @Test
    void updateService_inactive_throwsNotFound() {
        ShippingServiceEntity entity = ShippingServiceEntity.builder()
                .id(1L).code("EXPRESS_2H").active(false).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("X");
        request.setTariff(new BigDecimal("100"));
        request.setTotalCapacity(10);

        assertThatThrownBy(() -> catalogService.updateService(1L, request))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void deleteService_softDeletes() {
        ShippingServiceEntity entity = ShippingServiceEntity.builder()
                .id(1L).code("EXPRESS_2H").active(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        catalogService.deleteService(1L);

        ArgumentCaptor<ShippingServiceEntity> captor = ArgumentCaptor.forClass(ShippingServiceEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void deleteService_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.deleteService(1L))
                .isInstanceOf(ServiceNotFoundException.class);

        verify(repository, never()).save(any(ShippingServiceEntity.class));
    }
}