package com.rutaexpress.catalog.exception;

public class DuplicateServiceCodeException extends RuntimeException {

    public DuplicateServiceCodeException(String code) {
        super("Ya existe un servicio de envío con el código " + code);
    }
}