package com.rutaexpress.catalog.exception;

public class ServiceNotFoundException extends RuntimeException {

    public ServiceNotFoundException(Long id) {
        super("No se encontró el servicio de envío con id " + id);
    }
}