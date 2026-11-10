package com.repuestosanelisa;

import com.repuestosanelisa.util.Db;
import org.junit.*;

import java.sql.PreparedStatement;

import static org.junit.Assert.*;

/** prueba de rendimiento y estres con tiempos de carga y búsqueda con 2.000 productos. */
public class RendimientoTest extends BaseTest {

    private static final int CANTIDAD = 2000;

    @BeforeClass
    public static void antes() {
        limpiar();
    }

    @AfterClass
    public static void despues() {
        salir();
        limpiar();
    }

    // CP-22: con 2.000 productos, cargar y buscar debe tardar menos de 2 segundos
    @Test
    public void cp22_dosMilProductos() {
        entrarAdmin();
        Db.transaccion(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO productos (codigo, nombre, categoria, cantidad, precio) VALUES (?, ?, 'TSTPerf', 10, 100)")) {
                for (int i = 0; i < CANTIDAD; i++) {
                    ps.setString(1, "TST-P" + i);
                    ps.setString(2, "Producto de rendimiento " + i);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return null;
        });

        long inicio = System.currentTimeMillis();
        ctx().inventario.recargar();
        long carga = System.currentTimeMillis() - inicio;

        inicio = System.currentTimeMillis();
        int encontrados = ctx().inventario.buscar("rendimiento 1999", "Todas").size();
        long busqueda = System.currentTimeMillis() - inicio;

        System.out.println("RENDIMIENTO carga=" + carga + " ms, busqueda=" + busqueda + " ms");
        assertEquals(1, encontrados);
        assertTrue("La carga tardó " + carga + " ms", carga < 2000);
        assertTrue("La búsqueda tardó " + busqueda + " ms", busqueda < 2000);
    }
}
