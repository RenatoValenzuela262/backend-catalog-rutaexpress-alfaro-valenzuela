package com.rutaexpress.catalog.controller;

import com.rutaexpress.catalog.dto.request.CreateServiceRequest;
import com.rutaexpress.catalog.dto.request.UpdateServiceRequest;
import com.rutaexpress.catalog.dto.response.PageResponse;
import com.rutaexpress.catalog.dto.response.ServiceResponse;
import com.rutaexpress.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/services")
@RequiredArgsConstructor
@Tag(name = "Catálogo de servicios", description = "CRUD del catálogo de servicios de envío")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    @Operation(summary = "Lista paginada de servicios", description = "Por defecto solo devuelve servicios activos")
    public PageResponse<ServiceResponse> list(
            @Parameter(description = "Filtrar por activos. Si no se envía, asume true")
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalogService.listServices(active, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un servicio por id")
    public ServiceResponse get(@PathVariable Long id) {
        return catalogService.getService(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un servicio", description = "El código debe ser único. availableCapacity inicia igual a totalCapacity")
    public ServiceResponse create(@Valid @RequestBody CreateServiceRequest request) {
        return catalogService.createService(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un servicio (el código es inmutable)")
    public ServiceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateServiceRequest request) {
        return catalogService.updateService(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete de un servicio", description = "Marca active=false sin borrar la fila")
    public void delete(@PathVariable Long id) {
        catalogService.deleteService(id);
    }
}