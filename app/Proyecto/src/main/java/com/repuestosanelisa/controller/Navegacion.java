package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Modulo;

public final class Navegacion {
    private static MainController principal;

    private Navegacion() {}

    static void registrar(MainController mc) { principal = mc; }

    public static void ir(Modulo modulo) { if (principal != null) principal.ir(modulo); }

    public static void abrirFormularioProducto(Producto producto) {
        if (principal != null) principal.abrirFormularioProducto(producto);
    }
}
