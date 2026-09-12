package com.rutaexpress.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representación de un servicio de envío")
public class ServiceResponse {

    @Schema(description = "Identificador interno del servicio", example = "1")
    private Long id;

    @Schema(description = "Código único del servicio", example = "EXPRESS_2H")
    private String code;

    @Schema(description = "Nombre visible del servicio", example = "Express 2 horas")
    private String name;

    @Schema(description = "Descripción del servicio")
    private String description;

    @Schema(description = "Tarifa del servicio", example = "2500.00")
    private BigDecimal tariff;

    @Schema(description = "Capacidad total configurada", example = "100")
    private Integer totalCapacity;

    @Schema(description = "Capacidad disponible actual", example = "87")
    private Integer availableCapacity;

    @Schema(description = "Indica si el servicio está activo (soft-delete)", example = "true")
    private boolean active;

    @Schema(description = "Versión de bloqueo optimista", example = "4")
    private Long version;

    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;
}