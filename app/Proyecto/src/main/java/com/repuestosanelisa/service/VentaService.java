package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.model.Venta;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.util.Db;
import com.repuestosanelisa.util.NegocioException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VentaService {

    private final Inventario inventario;

    public VentaService(Inventario inventario) {
        this.inventario = inventario;
    }

    public Venta registrar(String cliente, List<LineaDetalle> lineas) {
        Permisos.exigir(Modulo.VENTAS);
        if (lineas.isEmpty()) throw new NegocioException("Agregue al menos un producto a la venta");

        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            if (p == null) throw new NegocioException("El producto " + l.getCodigo() + " ya no existe");
            if (l.getCantidad() <= 0) throw new NegocioException("Ingrese una cantidad válida");
            if (l.getCantidad() > p.getCantidad()) {
                throw new NegocioException("Stock insuficiente de " + p.getNombre()
                        + ": solo hay " + p.getCantidad() + " unidades");
            }
        }

        LocalDateTime fecha = LocalDateTime.now().withNano(0);
        String clienteFinal = cliente == null ? "" : cliente.trim();
        int id;
        try {
            id = Db.transaccion(c -> {
                int usuarioId = usuarioId(c, Sesion.actual().getUsuario());
                int ventaId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO ventas (fecha, usuario_id, cliente) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(fecha));
                    ps.setInt(2, usuarioId);
                    ps.setString(3, clienteFinal);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); ventaId = k.getInt(1); }
                }
                for (LineaDetalle l : lineas) {
                    // Descuenta solo si todavía hay stock: protege contra ventas simultáneas.
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE productos SET cantidad = cantidad - ? WHERE codigo = ? AND cantidad >= ?")) {
                        ps.setInt(1, l.getCantidad());
                        ps.setString(2, l.getCodigo());
                        ps.setInt(3, l.getCantidad());
                        if (ps.executeUpdate() == 0) {
                            throw new NegocioException("Stock insuficiente de " + l.getNombre()
                                    + ": el inventario cambió, revise las existencias");
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO venta_detalle (venta_id, producto_id, codigo, nombre, cantidad, precio_unitario) "
                                    + "VALUES (?, (SELECT id FROM productos WHERE codigo = ?), ?, ?, ?, ?)")) {
                        ps.setInt(1, ventaId);
                        ps.setString(2, l.getCodigo());
                        ps.setString(3, l.getCodigo());
                        ps.setString(4, l.getNombre());
                        ps.setInt(5, l.getCantidad());
                        ps.setDouble(6, l.getPrecioUnitario());
                        ps.executeUpdate();
                    }
                }
                return ventaId;
            });
        } catch (NegocioException e) {
            inventario.recargar();
            throw e;
        }


        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            if (p != null) p.setCantidad(p.getCantidad() - l.getCantidad());
        }
        return new Venta(id, fecha, Sesion.actual().getNombre(), clienteFinal, new ArrayList<>(lineas));
    }

    public List<Venta> listar() {
        return Db.consulta(c -> {
            Map<Integer, List<LineaDetalle>> detalle = new HashMap<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT venta_id, codigo, nombre, cantidad, precio_unitario FROM venta_detalle ORDER BY venta_id, id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalle.computeIfAbsent(rs.getInt(1), k -> new ArrayList<>())
                            .add(new LineaDetalle(rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5)));
                }
            }
            List<Venta> ventas = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT v.id, v.fecha, u.nombre, v.cliente FROM ventas v "
                            + "JOIN usuarios u ON u.id = v.usuario_id ORDER BY v.id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    ventas.add(new Venta(id, rs.getTimestamp(2).toLocalDateTime(), rs.getString(3), rs.getString(4),
                            detalle.getOrDefault(id, new ArrayList<>())));
                }
            }
            return ventas;
        });
    }

    static int usuarioId(java.sql.Connection c, String usuario) throws java.sql.SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM usuarios WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new NegocioException("El usuario de la sesión ya no existe");
                return rs.getInt(1);
            }
        }
    }
}
