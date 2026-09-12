package com.rutaexpress.catalog.service;

import com.rutaexpress.catalog.dto.request.CreateServiceRequest;
import com.rutaexpress.catalog.dto.request.UpdateServiceRequest;
import com.rutaexpress.catalog.dto.response.PageResponse;
import com.rutaexpress.catalog.dto.response.ServiceResponse;

public interface CatalogService {

    PageResponse<ServiceResponse> listServices(Boolean active, int page, int size);

    ServiceResponse getService(Long id);

    ServiceResponse createService(CreateServiceRequest request);

    ServiceResponse updateService(Long id, UpdateServiceRequest request);

    void deleteService(Long id);
}