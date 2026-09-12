package com.rutaexpress.catalog.mapper;

import com.rutaexpress.catalog.dto.request.CreateServiceRequest;
import com.rutaexpress.catalog.dto.response.ServiceResponse;
import com.rutaexpress.catalog.entities.ShippingServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    ServiceResponse toResponse(ShippingServiceEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "availableCapacity", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShippingServiceEntity toEntity(CreateServiceRequest request);
}