package com.rutaexpress.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Payload para actualizar un servicio de envío (el código es inmutable y no se expone)")
public class UpdateServiceRequest {

    @Schema(description = "Nombre visible del servicio", example = "Express 2 horas")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @Schema(description = "Descripción opcional", example = "Entrega en menos de 2 horas dentro de la misma ciudad")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;

    @Schema(description = "Tarifa del servicio", example = "2800.00")
    @NotNull(message = "La tarifa es obligatoria")
    @Positive(message = "La tarifa debe ser mayor a cero")
    @Digits(integer = 17, fraction = 2, message = "La tarifa solo admite hasta 2 decimales")
    private BigDecimal tariff;

    @Schema(description = "Nueva capacidad total. Si aumenta, la capacidad disponible aumenta en la misma diferencia; si disminuye, no puede quedar por debajo de la capacidad ya comprometida", example = "120")
    @NotNull(message = "La capacidad total es obligatoria")
    @Positive(message = "La capacidad total debe ser mayor a cero")
    private Integer totalCapacity;
}