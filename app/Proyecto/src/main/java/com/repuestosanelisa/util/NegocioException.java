package com.repuestosanelisa.util;

/** Error de validación o regla de negocio; el mensaje se muestra tal cual al usuario. */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensaje) { super(mensaje); }
}
