package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Inventario;

public final class Contexto {
    private static Contexto instancia;

    public final UsuarioService usuarios;
    public final ProveedorService proveedores;
    public final Inventario inventario;
    public final VentaService ventas;
    public final CompraService compras;
    public final ReporteService reportes;
    public final DashboardService dashboard;

    private Contexto() {
        usuarios = new UsuarioService();
        proveedores = new ProveedorService();
        inventario = new Inventario();
        ventas = new VentaService(inventario);
        compras = new CompraService(inventario, proveedores);
        reportes = new ReporteService(inventario, ventas, compras);
        dashboard = new DashboardService(inventario, ventas, compras);
    }

    public static synchronized Contexto get() {
        if (instancia == null) instancia = new Contexto();
        return instancia;
    }

    public static synchronized void reiniciar() { instancia = null; }
}