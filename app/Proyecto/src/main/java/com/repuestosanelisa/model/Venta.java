package com.repuestosanelisa.model;

import java.time.LocalDateTime;
import java.util.List;

public class Venta {
    private int id;
    private final LocalDateTime fecha;
    private final String usuario;
    private final String cliente;
    private final List<LineaDetalle> lineas;

    public Venta(int id, LocalDateTime fecha, String usuario, String cliente, List<LineaDetalle> lineas) {
        this.id = id; this.fecha = fecha; this.usuario = usuario; this.cliente = cliente; this.lineas = lineas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public String getUsuario() { return usuario; }
    public String getCliente() { return cliente; }
    public List<LineaDetalle> getLineas() { return lineas; }
    public double getTotal() { return lineas.stream().mapToDouble(LineaDetalle::getSubtotal).sum(); }
}
