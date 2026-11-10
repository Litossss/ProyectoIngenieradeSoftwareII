package com.repuestosanelisa;

import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.util.NegocioException;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Pruebas que muestran los defectos encontrados
 */
public class DefectosConocidos extends BaseTest {

    @BeforeClass
    public static void antes() {
        limpiar();
        entrarAdmin();
    }

    @AfterClass
    public static void despues() {
        salir();
        limpiar();
    }

    // CP-26 (DEF-001): un producto con un proveedor que no existe debería rechazarse

    @Test
    public void cp26_productoConProveedorInexistente() {
        try {
            ctx().inventario.agregarProducto(new Producto("TST-D01", "Con proveedor falso", "TSTCat", 1, 1, "Proveedor que no existe", "", ""));
            fail("DEF-001: se guardó un producto con un proveedor que no existe");
        } catch (NegocioException e) {
            // correcto: se rechazó
        }
    }

    // CP-27 (DEF-002): un error de SQL no debería mostrarse como "base de datos desactualizada"

    @Test
    public void cp27_mensajeDeErrorSql() {
        try {
            ctx().inventario.agregarProducto(new Producto("TST-D02", "Precio NaN", "TSTCat", 1, Double.NaN, "Proveedor 1", "", ""));
            fail("Debía rechazar el precio NaN");
        } catch (NegocioException e) {
            assertFalse("DEF-002: " + e.getMessage(), e.getMessage().contains("desactualizada"));
        }
    }
}
