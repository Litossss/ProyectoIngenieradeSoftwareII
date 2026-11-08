package com.repuestosanelisa.model;

import java.time.LocalDateTime;
import java.util.List;

public class Compra {
    private int id;
    private final LocalDateTime fecha;
    private final String proveedor;
    private final String usuario;
    private final List<LineaDetalle> lineas;

    public Compra(int id, LocalDateTime fecha, String proveedor, String usuario, List<LineaDetalle> lineas) {
        this.id = id; this.fecha = fecha; this.proveedor = proveedor; this.usuario = usuario; this.lineas = lineas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public String getProveedor() { return proveedor; }
    public String getUsuario() { return usuario; }
    public List<LineaDetalle> getLineas() { return lineas; }
    public double getTotal() { return lineas.stream().mapToDouble(LineaDetalle::getSubtotal).sum(); }
}
