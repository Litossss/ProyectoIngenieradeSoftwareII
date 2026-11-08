package com.repuestosanelisa.model;

public class LineaDetalle {
    private final String codigo;
    private final String nombre;
    private int cantidad;
    private final double precioUnitario;

    public LineaDetalle(String codigo, String nombre, int cantidad, double precioUnitario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getSubtotal() { return cantidad * precioUnitario; }
}
