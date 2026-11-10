package com.repuestosanelisa;

import com.repuestosanelisa.model.TipoReporte;
import com.repuestosanelisa.util.NegocioException;
import com.repuestosanelisa.util.PdfUtil;
import org.junit.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/** pruebas de proveedores y reportes. */
public class ProveedoresReportesTest extends BaseTest {

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

    // CP-20: registrar un proveedor y rechazar uno repetido
    @Test
    public void cp20_registrarProveedor() {
        ctx().proveedores.registrar("TST Prov Uno", "3000000000");
        assertTrue(ctx().proveedores.estaActivo("TST Prov Uno"));
        try {
            ctx().proveedores.registrar("TST Prov Uno", "otro");
            fail("Debía rechazar el proveedor repetido");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("ya está registrado"));
        }
    }

    // CP-21: el reporte de inventario se guarda como un PDF válido
    @Test
    public void cp21_reporteInventarioPdf() throws Exception {
        String texto = ctx().reportes.generar(TipoReporte.INVENTARIO, null, null, "Todas");
        assertTrue(texto.contains("Reporte de inventario"));
        Path archivo = Files.createTempFile("reporte-prueba", ".pdf");
        PdfUtil.guardar(archivo, texto);
        byte[] bytes = Files.readAllBytes(archivo);
        assertTrue(bytes.length > 500);
        // todo PDF empieza con "%PDF"
        assertEquals("%PDF", new String(bytes, 0, 4, "ISO-8859-1"));
        Files.delete(archivo);
    }
}
