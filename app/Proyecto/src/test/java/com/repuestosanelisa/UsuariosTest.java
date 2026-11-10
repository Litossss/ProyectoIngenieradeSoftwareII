package com.repuestosanelisa;

import com.repuestosanelisa.model.Rol;
import com.repuestosanelisa.model.Usuario;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.util.Db;
import com.repuestosanelisa.util.NegocioException;
import org.junit.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.*;

/** Pruebas de usuario y seguridad */
public class UsuariosTest extends BaseTest {

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
    public void sinSesion() {
        salir();
    }

    // CP-01: iniciar sesión como administrador
    @Test
    public void cp01_loginAdministrador() {
        Usuario u = ctx().usuarios.login("anelisa", "admin123");
        assertEquals(Rol.ADMINISTRADOR, u.getRol());
        assertNotNull(Sesion.actual());
    }

    // CP-02: contraseña incorrecta
    @Test
    public void cp02_contrasenaIncorrecta() {
        try {
            ctx().usuarios.login("anelisa", "mala");
            fail("Debía rechazar la contraseña");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("Credenciales incorrectas"));
        }
        assertNull(Sesion.actual());
    }

    // CP-03: campos vacíos
    @Test(expected = NegocioException.class)
    public void cp03_camposVacios() {
        ctx().usuarios.login("", "");
    }

    // CP-04: inyección SQL en el inicio de sesión
    @Test
    public void cp04_inyeccionSql() {
        try {
            ctx().usuarios.login("' OR '1'='1", "' OR '1'='1");
            fail("No debía dejar entrar");
        } catch (NegocioException e) {
            assertNull(Sesion.actual());
        }
        // la tabla sigue existiendo y el login normal funciona
        assertEquals(Rol.ADMINISTRADOR, ctx().usuarios.login("anelisa", "admin123").getRol());
    }

    // CP-05 (parte con la base): lo guardado en la tabla usuarios es un hash de 64 caracteres
    @Test
    public void cp05_hashGuardadoEnLaBase() {
        String hash = Db.consulta(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT contrasena_hash FROM usuarios WHERE usuario = 'anelisa'");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        });
        assertEquals(64, hash.length());
        assertNotEquals("admin123", hash);
    }

    // CP-06: el administrador crea un usuario empleado y ese usuario puede entrar
    @Test
    public void cp06_crearUsuario() {
        entrarAdmin();
        ctx().usuarios.crear("Prueba Uno", "tst_uno", "clave1", Rol.EMPLEADO, true);
        salir();
        Usuario u = ctx().usuarios.login("tst_uno", "clave1");
        assertEquals("Prueba Uno", u.getNombre());
        assertEquals(Rol.EMPLEADO, u.getRol());
    }

    // CP-07: no se puede repetir el nombre de usuario
    @Test
    public void cp07_usuarioDuplicado() {
        entrarAdmin();
        ctx().usuarios.crear("Dup", "tst_dup", "clave1", Rol.EMPLEADO, true);
        try {
            ctx().usuarios.crear("Otro", "tst_dup", "clave2", Rol.EMPLEADO, true);
            fail("Debía rechazar el usuario repetido");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("ya existe"));
        }
    }

    // CP-08: el empleado no puede gestionar usuarios
    @Test
    public void cp08_empleadoNoGestionaUsuarios() {
        entrarEmpleado();
        try {
            ctx().usuarios.crear("X", "tst_emp", "clave1", Rol.EMPLEADO, true);
            fail("El empleado no debía poder crear usuarios");
        } catch (NegocioException e) {
            assertTrue(e.getMessage().contains("No tiene permiso"));
        }
    }
}
