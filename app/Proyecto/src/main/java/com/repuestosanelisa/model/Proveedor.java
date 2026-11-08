package com.repuestosanelisa.model;

public class Proveedor {
    private final String nombre;
    private final String contacto;
    private final boolean activo;

    public Proveedor(String nombre, String contacto) { this(nombre, contacto, true); }

    public Proveedor(String nombre, String contacto, boolean activo) {
        this.nombre = nombre;
        this.contacto = contacto;
        this.activo = activo;
    }

    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
    public boolean isActivo() { return activo; }
    public String getEstado() { return activo ? "Activo" : "Inactivo"; }
}
