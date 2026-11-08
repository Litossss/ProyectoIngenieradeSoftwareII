package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.util.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Persistencia de productos (RF01, RF05) en la tabla productos de MySQL. */
public class InventarioService {

    public List<Producto> cargarProductos() {
        return Db.consulta(c -> {
            List<Producto> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT p.codigo, p.nombre, p.categoria, p.cantidad, p.precio, "
                            + "COALESCE(pr.nombre, ''), COALESCE(p.descripcion, ''), p.foto "
                            + "FROM productos p LEFT JOIN proveedores pr ON pr.id = p.proveedor_id "
                            + "ORDER BY p.codigo");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Producto(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                            rs.getDouble(5), rs.getString(6), rs.getString(7), rs.getString(8)));
                }
            }
            return lista;
        });
    }

    public void insertar(Producto p) {
        Db.consulta(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO productos (codigo, nombre, categoria, cantidad, precio, proveedor_id, descripcion, foto) "
                            + "VALUES (?, ?, ?, ?, ?, (SELECT id FROM proveedores WHERE nombre = ?), ?, ?)")) {
                ps.setString(1, p.getCodigo());
                ps.setString(2, p.getNombre());
                ps.setString(3, p.getCategoria());
                ps.setInt(4, p.getCantidad());
                ps.setDouble(5, p.getPrecio());
                ps.setString(6, p.getProveedor());
                ps.setString(7, p.getDescripcion());
                ps.setString(8, p.getFoto());
                ps.executeUpdate();
            }
            return null;
        });
    }

    public void actualizar(Producto p) {
        Db.consulta(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE productos SET nombre = ?, categoria = ?, cantidad = ?, precio = ?, "
                            + "proveedor_id = (SELECT id FROM proveedores WHERE nombre = ?), descripcion = ?, foto = ? "
                            + "WHERE codigo = ?")) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getCategoria());
                ps.setInt(3, p.getCantidad());
                ps.setDouble(4, p.getPrecio());
                ps.setString(5, p.getProveedor());
                ps.setString(6, p.getDescripcion());
                ps.setString(7, p.getFoto());
                ps.setString(8, p.getCodigo());
                ps.executeUpdate();
            }
            return null;
        });
    }

    public void eliminar(String codigo) {
        Db.consulta(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM productos WHERE codigo = ?")) {
                ps.setString(1, codigo);
                ps.executeUpdate();
            }
            return null;
        });
    }
}
