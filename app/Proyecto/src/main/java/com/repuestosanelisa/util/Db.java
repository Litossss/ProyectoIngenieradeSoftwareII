package com.repuestosanelisa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Acceso a MySQL por JDBC. Toda consulta pasa por aquí: abre la conexión, la cierra
 * y convierte los errores SQL en NegocioException con un mensaje entendible.
 */
public final class Db {
    private Db() {}

    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }

    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);
    }

    /** Comprueba la conexión al iniciar la aplicación. */
    public static void verificar() {
        consulta(c -> c.isValid(5));
    }

    /** Ejecuta una o varias lecturas / escrituras sueltas (cada sentencia se confirma sola). */
    public static <T> T consulta(Trabajo<T> trabajo) {
        try (Connection c = abrir()) {
            return trabajo.ejecutar(c);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    /**
     * Ejecuta todo dentro de UNA transacción: si algo falla (SQL o NegocioException)
     * se hace rollback y no queda nada a medias.
     */
    public static <T> T transaccion(Trabajo<T> trabajo) {
        try (Connection c = abrir()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException ignorada) { /* se informa el error original */ }
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    private static NegocioException traducir(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String detalle = e.getMessage() == null ? "" : e.getMessage();
        if (detalle.contains("No suitable driver")) {
            return new NegocioException("Falta el driver de MySQL (mysql-connector-j). En IntelliJ: pestaña Maven > "
                    + "Reload All Maven Projects, y vuelva a ejecutar.");
        }
        if (estado.startsWith("08") || e.getErrorCode() == 0 && detalle.toLowerCase().contains("communications link")) {
            return new NegocioException("No se pudo conectar con MySQL (" + Config.DB_URL.split("\\?")[0]
                    + "). Verifique que el servidor esté encendido y que el script de la base de datos se haya ejecutado."
                    + "\n\nDetalle técnico: " + detalle);
        }
        if (e.getErrorCode() == 1045 || "28000".equals(estado)) {
            return new NegocioException("MySQL rechazó el usuario o la contraseña de la aplicación (" + Config.DB_USER + ").");
        }
        if (e.getErrorCode() == 1044) {
            return new NegocioException("El usuario " + Config.DB_USER + " no tiene permiso sobre la base de datos o esta no existe. Ejecute el script de configuracion-mysql.txt.");
        }
        if (e.getErrorCode() == 1049) {
            return new NegocioException("La base de datos no existe. Ejecute el script de configuracion-mysql.txt.");
        }
        if (e.getErrorCode() == 1062) {
            return new NegocioException("Ya existe un registro con esos datos.");
        }
        if (e.getErrorCode() == 1146) {
            return new NegocioException("Faltan tablas en la base de datos. Ejecute el script de configuracion-mysql.txt.");
        }
        return new NegocioException("Error de base de datos: " + e.getMessage());
    }
}
