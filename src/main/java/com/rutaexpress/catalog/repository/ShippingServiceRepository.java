package com.rutaexpress.catalog.repository;

import com.rutaexpress.catalog.entities.ShippingServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingServiceRepository extends JpaRepository<ShippingServiceEntity, Long> {

    boolean existsByCode(String code);

    Optional<ShippingServiceEntity> findByCode(String code);

    Page<ShippingServiceEntity> findByActive(boolean active, Pageable pageable);
}