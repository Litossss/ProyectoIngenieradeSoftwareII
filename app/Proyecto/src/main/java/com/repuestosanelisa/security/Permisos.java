package com.repuestosanelisa.security;

import com.repuestosanelisa.model.Rol;
import com.repuestosanelisa.model.Usuario;
import com.repuestosanelisa.util.NegocioException;

/** Matriz de permisos (Pantalla 9). La interfaz oculta lo que no corresponde y los servicios lo vuelven a exigir. */
public final class Permisos {
    private Permisos() {}

    public static boolean puede(Rol rol, Modulo m) {
        return switch (m) {
            case INICIO, INVENTARIO, VENTAS -> true;
            case COMPRAS, PROVEEDORES, REPORTES, USUARIOS -> rol == Rol.ADMINISTRADOR;
        };
    }

    public static boolean esAdmin() {
        Usuario u = Sesion.actual();
        return u != null && u.getRol() == Rol.ADMINISTRADOR;
    }

    public static void exigir(Modulo m) {
        Usuario u = Sesion.actual();
        if (u == null) throw new NegocioException("Debe iniciar sesión");
        if (!puede(u.getRol(), m)) throw new NegocioException("No tiene permiso para realizar esta operación");
    }

    public static void exigirAdmin() {
        if (!esAdmin()) throw new NegocioException("Solo el Administrador puede realizar esta operación");
    }
}
