package com.repuestosanelisa.security;

import com.repuestosanelisa.model.Usuario;

/** Usuario autenticado actualmente. */
public final class Sesion {
    private static Usuario actual;
    private Sesion() {}

    public static void iniciar(Usuario u) { actual = u; }
    public static void cerrar() { actual = null; }
    public static Usuario actual() { return actual; }
}
