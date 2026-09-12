package com.rutaexpress.catalog.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHIPPING_SERVICES")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipping_services_seq")
    @SequenceGenerator(name = "shipping_services_seq", sequenceName = "SHIPPING_SERVICES_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODE", nullable = false, unique = true, length = 50, updatable = false)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "TARIFF", nullable = false, precision = 19, scale = 2)
    private BigDecimal tariff;

    @Column(name = "TOTAL_CAPACITY", nullable = false)
    private Integer totalCapacity;

    @Column(name = "AVAILABLE_CAPACITY", nullable = false)
    private Integer availableCapacity;

    @Column(name = "ACTIVE", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}