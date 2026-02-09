package com.example.checklistapp.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String recurso;
    private final Object identificador;

    public ResourceNotFoundException(String mensaje, String recurso, Object identificador) {
        super(mensaje);
        this.recurso = recurso;
        this.identificador = identificador;
    }

    public ResourceNotFoundException(String recurso, Object id) {
        this(String.format("%s no encontrado(a) con id: %s", recurso, id), recurso, id);
    }

    public ResourceNotFoundException(String mensaje) {
        this(mensaje, null, null);
    }

    public String getRecurso() {
        return recurso;
    }

    public Object getIdentificador() {
        return identificador;
    }
}
