package com.rutaexpress.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Formato estándar de error de la API")
public class ErrorResponse {

    @Schema(description = "Marca de tiempo del error", example = "2026-09-12T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "Código HTTP", example = "409")
    private int status;

    @Schema(description = "Frase estándar del código HTTP", example = "Conflict")
    private String error;

    @Schema(description = "Mensaje legible del error", example = "Capacidad insuficiente para el servicio EXPRESS_2H")
    private String message;

    @Schema(description = "Ruta del endpoint que generó el error", example = "/api/catalog/services/3/capacity/decrease")
    private String path;
}