package com.repuestosanelisa;

import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.util.NegocioException;
import org.junit.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/** ventas y compras: Se comprueba el stock leyendo de nuevo desde MySQL. */
public class VentasComprasTest extends BaseTest {

    private static final String PROVEEDOR = "TST Proveedor";

    private void crearProducto(String codigo, int cantidad, double precio) {
        ctx().inventario.agregarProducto(new Producto(codigo, "Prod " + codigo, "TSTVentas", cantidad, precio, "Proveedor 1", "", ""));
    }

    private LineaDetalle linea(String codigo, int cantidad, double precio) {
        return new LineaDetalle(codigo, "Prod " + codigo, cantidad, precio);
    }

    private int stock(String codigo) {
        ctx().inventario.recargar();
        return ctx().inventario.buscarProducto(codigo).getCantidad();
    }

    @BeforeClass
    public static void antes() {
        limpiar();
        entrarAdmin();
        ctx().proveedores.registrar(PROVEEDOR, "prueba");
    }

    @AfterClass
    public static void despues() {
        salir();
        limpiar();
    }

    @Before
    public void admin() {
        entrarAdmin();
    }

    // CP-17: una venta descuenta el stock
    @Test
    public void cp17_ventaDescuentaStock() {
        crearProducto("TST-V01", 10, 1000);
        ctx().ventas.registrar("TST-cliente", List.of(linea("TST-V01", 3, 1000)));
        assertEquals(7, stock("TST-V01"));
    }

    // CP-18: vender más de lo que hay no se permite y el stock no cambia
    @Test
    public void cp18_ventaMayorAlStock() {
        crearProducto("TST-V02", 4, 500);
        try {
            ctx().ventas.registrar("TST-cliente", List.of(linea("TST-V02", 5, 500)));
            fail("Debía rechazar la venta");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("Stock insuficiente"));
        }
        assertEquals(4, stock("TST-V02"));
    }

    // CP-19: una compra aumenta el stock
    @Test
    public void cp19_compraAumentaStock() {
        crearProducto("TST-C01", 2, 100);
        ctx().compras.registrar(PROVEEDOR, LocalDate.now(), List.of(linea("TST-C01", 8, 80)));
        assertEquals(10, stock("TST-C01"));
    }
}
