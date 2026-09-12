package com.rutaexpress.catalog.service;

public interface CapacityService {

    /**
     * Descuenta {@code amount} de la capacidad disponible. Usado por
     * ms-rutaexpress-shipments cuando un envío pasa a estado ACEPTADO.
     */
    void decrease(Long serviceId, Integer amount);

    /**
     * Repone {@code amount} a la capacidad disponible sin exceder la capacidad
     * total. Usado al cancelar un envío ya aceptado.
     */
    void increase(Long serviceId, Integer amount);
}