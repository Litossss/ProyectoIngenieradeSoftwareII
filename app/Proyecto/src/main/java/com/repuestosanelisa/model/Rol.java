package com.repuestosanelisa.model;

public enum Rol {
    ADMINISTRADOR("Administrador", "inventario, ventas, compras, reportes y usuarios."),
    EMPLEADO("Empleado", "consultar existencias, registrar ventas e ingresar o actualizar productos.");

    private final String etiqueta;
    private final String permisos;

    Rol(String etiqueta, String permisos) { this.etiqueta = etiqueta; this.permisos = permisos; }

    public String getPermisos() { return permisos; }

    @Override public String toString() { return etiqueta; }
}
