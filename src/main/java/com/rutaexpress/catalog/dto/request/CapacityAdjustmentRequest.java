package com.rutaexpress.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Payload para ajustar la capacidad disponible de un servicio (decrease/increase)")
public class CapacityAdjustmentRequest {

    @Schema(description = "Cantidad de capacidad a descontar o reponer", example = "5")
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private Integer amount;
}