package com.repuestosanelisa;

import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Db;

import java.sql.PreparedStatement;

/**
 * Clase para base de las pruebas que usan la base de datos en general.
 * crea productos con TST- haciendo referencia a test y se borra al terminar.
 */
public class BaseTest {

    protected static Contexto ctx() {
        return Contexto.get();
    }

    protected static void entrarAdmin() {
        Sesion.cerrar();
        ctx().usuarios.login("anelisa", "admin123");
    }

    protected static void entrarEmpleado() {
        Sesion.cerrar();
        ctx().usuarios.login("empleado1", "emp123");
    }

    protected static void salir() {
        Sesion.cerrar();
    }

    protected static void limpiar() {
        String[] borrar = {
            "DELETE FROM venta_detalle WHERE codigo LIKE 'TST-%'",
            "DELETE FROM ventas WHERE cliente LIKE 'TST-%'",
            "DELETE FROM compra_detalle WHERE codigo LIKE 'TST-%'",
            "DELETE FROM compras WHERE proveedor_id IN (SELECT id FROM proveedores WHERE nombre LIKE 'TST %')",
            "DELETE FROM productos WHERE codigo LIKE 'TST-%'",
            "DELETE FROM proveedores WHERE nombre LIKE 'TST %'",
            "DELETE FROM usuarios WHERE usuario LIKE 'tst\\_%'"
        };
        for (String sql : borrar) {
            Db.consulta(c -> {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.executeUpdate();
                }
                return null;
            });
        }
        ctx().inventario.recargar();
    }
}
