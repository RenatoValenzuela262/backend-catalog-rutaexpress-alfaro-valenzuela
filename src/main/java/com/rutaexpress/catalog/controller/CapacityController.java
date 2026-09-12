package com.rutaexpress.catalog.controller;

import com.rutaexpress.catalog.dto.request.CapacityAdjustmentRequest;
import com.rutaexpress.catalog.service.CapacityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/services/{id}/capacity")
@RequiredArgsConstructor
@Tag(name = "Capacidad", description = "Ajustes de capacidad disponible de los servicios")
public class CapacityController {

    private final CapacityService capacityService;

    @PatchMapping("/decrease")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Descuenta capacidad disponible",
            description = "Usado por ms-rutaexpress-shipments cuando un envío pasa a ACEPTADO. HTTP 409 si el monto supera la capacidad disponible")
    public void decrease(@PathVariable Long id, @Valid @RequestBody CapacityAdjustmentRequest request) {
        capacityService.decrease(id, request.getAmount());
    }

    @PatchMapping("/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Repone capacidad disponible",
            description = "Usado al cancelar un envío ya aceptado. No excede la capacidad total del servicio")
    public void increase(@PathVariable Long id, @Valid @RequestBody CapacityAdjustmentRequest request) {
        capacityService.increase(id, request.getAmount());
    }
}