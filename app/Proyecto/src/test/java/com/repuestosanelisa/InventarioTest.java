package com.repuestosanelisa;

import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.util.Config;
import com.repuestosanelisa.util.NegocioException;
import org.junit.*;

import java.util.List;

import static org.junit.Assert.*;

/** Inventario de pruebas. */
public class InventarioTest extends BaseTest {

    private Inventario inv() {
        return ctx().inventario;
    }

    private Producto producto(String codigo, String nombre, int cantidad, double precio) {
        return new Producto(codigo, nombre, "TSTCat", cantidad, precio, "Proveedor 1", "Prueba", "");
    }

    @BeforeClass
    public static void antes() {
        limpiar();
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

    // CP-10: agregar un producto válido
    @Test
    public void cp10_agregarProducto() {
        inv().agregarProducto(producto("TST-001", "Bujía de prueba", 10, 5000));
        Producto p = inv().buscarProducto("TST-001");
        assertNotNull(p);
        assertEquals(10, p.getCantidad());
        assertEquals(5000, p.getPrecio(), 0);
    }

    // CP-11: no se permite repetir el código
    @Test
    public void cp11_codigoDuplicado() {
        inv().agregarProducto(producto("TST-002", "Uno", 1, 1));
        try {
            inv().agregarProducto(producto("TST-002", "Otro", 1, 1));
            fail("Debía rechazar el código repetido");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("ya existe"));
        }
    }

    // CP-12: cantidad o precio negativos
    @Test
    public void cp12_valoresNegativos() {
        try {
            inv().agregarProducto(producto("TST-003", "Negativo", -1, 10));
            fail("Debía rechazar la cantidad negativa");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("no pueden ser negativos"));
        }
        try {
            inv().agregarProducto(producto("TST-003", "Negativo", 1, -5));
            fail("Debía rechazar el precio negativo");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("no pueden ser negativos"));
        }
        assertNull(inv().buscarProducto("TST-003"));
    }

    // CP-13: editar un producto y ver el cambio guardado en la base
    @Test
    public void cp13_editarProducto() {
        inv().agregarProducto(producto("TST-004", "Original", 5, 100));
        inv().actualizarProducto(producto("TST-004", "Editado", 7, 150));
        inv().recargar(); // vuelve a leer desde MySQL
        Producto p = inv().buscarProducto("TST-004");
        assertEquals("Editado", p.getNombre());
        assertEquals(7, p.getCantidad());
        assertEquals(150, p.getPrecio(), 0);
    }

    // CP-14: el administrador elimina un producto
    @Test
    public void cp14_eliminarProducto() {
        inv().agregarProducto(producto("TST-005", "Para borrar", 1, 1));
        inv().eliminarProducto(inv().buscarProducto("TST-005"));
        inv().recargar();
        assertNull(inv().buscarProducto("TST-005"));
    }

    // CP-15: buscar por nombre (sin importar mayúsculas)
    @Test
    public void cp15_buscarPorNombre() {
        inv().agregarProducto(producto("TST-006", "Filtro de aire TSTX", 4, 10));
        List<Producto> r = inv().buscar("filtro de AIRE tstx", "Todas");
        assertEquals(1, r.size());
        assertEquals("TST-006", r.get(0).getCodigo());
        // una búsqueda sin coincidencias devuelve lista vacía
        assertTrue(inv().buscar("zzzz-no-existe", "Todas").isEmpty());
    }

    // CP-16: alerta de poco stock (límite: el umbral y una unidad más)
    @Test
    public void cp16_alertaPocoStock() {
        int umbral = Config.UMBRAL_POCO_STOCK;
        inv().agregarProducto(producto("TST-007", "En el umbral", umbral, 1));
        inv().agregarProducto(producto("TST-008", "Sobre el umbral", umbral + 1, 1));
        List<String> bajos = ctx().dashboard.pocoStock(1000).stream().map(Producto::getCodigo).toList();
        assertTrue(bajos.contains("TST-007"));
        assertFalse(bajos.contains("TST-008"));
    }
}
