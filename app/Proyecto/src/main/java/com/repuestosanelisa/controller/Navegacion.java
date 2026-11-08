package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Modulo;

/** Puente para que las vistas hijas pidan cambiar de pantalla sin conocer MainController. */
public final class Navegacion {
    private static MainController principal;

    private Navegacion() {}

    static void registrar(MainController mc) { principal = mc; }

    public static void ir(Modulo modulo) { if (principal != null) principal.ir(modulo); }

    /** Abre el formulario: producto == null para registrar uno nuevo. */
    public static void abrirFormularioProducto(Producto producto) {
        if (principal != null) principal.abrirFormularioProducto(producto);
    }
}
