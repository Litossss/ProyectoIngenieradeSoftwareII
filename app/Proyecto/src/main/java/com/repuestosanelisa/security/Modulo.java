package com.repuestosanelisa.security;

public enum Modulo {
    INICIO("Inicio"), INVENTARIO("Inventario"), VENTAS("Ventas"),
    COMPRAS("Compras"), PROVEEDORES("Proveedores"), REPORTES("Reportes"), USUARIOS("Usuarios");

    private final String etiqueta;
    Modulo(String etiqueta) { this.etiqueta = etiqueta; }

    @Override public String toString() { return etiqueta; }
}
