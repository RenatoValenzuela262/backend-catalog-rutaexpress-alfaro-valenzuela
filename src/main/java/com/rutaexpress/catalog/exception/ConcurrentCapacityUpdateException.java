package com.rutaexpress.catalog.exception;

/**
 * Se lanza cuando el ajuste de capacidad no pudo completarse tras reintentar
 * el bloqueo optimista ({@code @Version}): dos transacciones compiten por
 * actualizar la misma entidad y la contienda persiste, por lo que el API
 * responde HTTP 409 Conflict.
 */
public class ConcurrentCapacityUpdateException extends RuntimeException {

    public ConcurrentCapacityUpdateException(Long serviceId) {
        super("Conflicto de concurrencia al ajustar la capacidad del servicio con id " + serviceId);
    }
}