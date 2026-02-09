package com.example.checklistapp.common.exception;

public class BusinessRuleException extends RuntimeException {

    private final String regla;

    public BusinessRuleException(String mensaje) {
        super(mensaje);
        this.regla = mensaje;
    }

    public BusinessRuleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.regla = mensaje;
    }

    public BusinessRuleException(String mensaje, String regla) {
        super(mensaje);
        this.regla = regla;
    }

    public String getRegla() {
        return regla;
    }
}
