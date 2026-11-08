package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Proveedor;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.util.Db;
import com.repuestosanelisa.util.NegocioException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class ProveedorService {

    public List<String> nombres() {
        return Db.consulta(c -> {
            List<String> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT nombre FROM proveedores WHERE activo = 1 ORDER BY nombre");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(rs.getString(1));
            }
            return lista;
        });
    }

    public List<Proveedor> listar() {
        Permisos.exigir(Modulo.PROVEEDORES);
        return Db.consulta(c -> {
            List<Proveedor> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT nombre, contacto, activo FROM proveedores ORDER BY nombre");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new Proveedor(rs.getString(1), rs.getString(2), rs.getBoolean(3)));
            }
            return lista;
        });
    }

    public boolean existe(String nombre) {
        return consultarExiste("SELECT 1 FROM proveedores WHERE nombre = ?", nombre);
    }

    public boolean estaActivo(String nombre) {
        return consultarExiste("SELECT 1 FROM proveedores WHERE nombre = ? AND activo = 1", nombre);
    }

    private boolean consultarExiste(String sql, String nombre) {
        return Db.consulta(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, nombre);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            }
        });
    }

    public void registrar(String nombre, String contacto) {
        Permisos.exigir(Modulo.PROVEEDORES);
        if (nombre == null || nombre.isBlank()) throw new NegocioException("Ingrese el nombre del proveedor");
        if (existe(nombre.trim())) throw new NegocioException("El proveedor ya está registrado");
        Db.consulta(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO proveedores (nombre, contacto) VALUES (?, ?)")) {
                ps.setString(1, nombre.trim());
                ps.setString(2, limpiar(contacto));
                ps.executeUpdate();
            }
            return null;
        });
    }

    public void editar(String nombreActual, String nuevoNombre, String contacto, boolean activo) {
        Permisos.exigir(Modulo.PROVEEDORES);
        if (nuevoNombre == null || nuevoNombre.isBlank()) throw new NegocioException("Ingrese el nombre del proveedor");
        String nombre = nuevoNombre.trim();
        Db.transaccion(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM proveedores WHERE nombre = ? AND nombre <> ?")) {
                ps.setString(1, nombre);
                ps.setString(2, nombreActual);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) throw new NegocioException("Ya existe otro proveedor con ese nombre");
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE proveedores SET nombre = ?, contacto = ?, activo = ? WHERE nombre = ?")) {
                ps.setString(1, nombre);
                ps.setString(2, limpiar(contacto));
                ps.setBoolean(3, activo);
                ps.setString(4, nombreActual);
                if (ps.executeUpdate() == 0) throw new NegocioException("El proveedor ya no existe");
            }
            return null;
        });
    }

    private static String limpiar(String texto) { return texto == null ? "" : texto.trim(); }
}
