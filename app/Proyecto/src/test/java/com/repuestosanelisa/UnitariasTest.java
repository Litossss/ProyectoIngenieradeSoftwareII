package com.repuestosanelisa;

import com.repuestosanelisa.model.Rol;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.util.HashUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/** Pruebas unitarias de forma interna. sin base de datos. */
public class UnitariasTest {

    // CP-05: la contraseña se guarda con SHA-256
    @Test
    public void cp05_hashSha256() {
        // valor conocido de SHA-256 para "abc"
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", HashUtil.sha256("abc"));
        // siempre da 64 caracteres y no es la contraseña original
        assertEquals(64, HashUtil.sha256("admin123").length());
        assertNotEquals("admin123", HashUtil.sha256("admin123"));
        // mayúsculas distintas dan otro hash
        assertNotEquals(HashUtil.sha256("admin123"), HashUtil.sha256("Admin123"));
    }

    // CP-09: permisos por rol
    @Test
    public void cp09_permisosPorRol() {
        // el administrador entra a todos los módulos
        for (Modulo m : Modulo.values()) {
            assertTrue(Permisos.puede(Rol.ADMINISTRADOR, m));
        }
        // el empleado solo a Inicio, Inventario y Ventas
        assertTrue(Permisos.puede(Rol.EMPLEADO, Modulo.INICIO));
        assertTrue(Permisos.puede(Rol.EMPLEADO, Modulo.INVENTARIO));
        assertTrue(Permisos.puede(Rol.EMPLEADO, Modulo.VENTAS));
        assertFalse(Permisos.puede(Rol.EMPLEADO, Modulo.COMPRAS));
        assertFalse(Permisos.puede(Rol.EMPLEADO, Modulo.PROVEEDORES));
        assertFalse(Permisos.puede(Rol.EMPLEADO, Modulo.REPORTES));
        assertFalse(Permisos.puede(Rol.EMPLEADO, Modulo.USUARIOS));
    }
}
