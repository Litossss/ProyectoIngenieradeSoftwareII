package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Compra;
import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.util.Db;
import com.repuestosanelisa.util.NegocioException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF01/RF02 - entradas (solo Administrador). La compra es UNA transacción en MySQL:
 * INSERT compra + INSERT detalle + UPDATE de stock; si algo falla se hace rollback completo.
 */
public class CompraService {

    private final Inventario inventario;
    private final ProveedorService proveedores;

    public CompraService(Inventario inventario, ProveedorService proveedores) {
        this.inventario = inventario;
        this.proveedores = proveedores;
    }

    public Compra registrar(String proveedor, LocalDate fecha, List<LineaDetalle> lineas) {
        Permisos.exigir(Modulo.COMPRAS);
        if (proveedor == null || proveedor.isBlank()) throw new NegocioException("Indique el proveedor");
        if (!proveedores.estaActivo(proveedor.trim())) {
            throw new NegocioException("El proveedor no está registrado o está desactivado");
        }
        if (lineas.isEmpty()) throw new NegocioException("Agregue al menos un producto a la compra");
        for (LineaDetalle l : lineas) {
            if (inventario.buscarProducto(l.getCodigo()) == null) {
                throw new NegocioException("El producto " + l.getCodigo() + " no está registrado");
            }
            if (l.getCantidad() <= 0) throw new NegocioException("Ingrese una cantidad válida");
        }

        LocalDate f = fecha == null ? LocalDate.now() : fecha;
        LocalDateTime momento = LocalDateTime.of(f, LocalTime.now().withNano(0));
        String proveedorFinal = proveedor.trim();
        int id;
        try {
            id = Db.transaccion(c -> {
                int usuarioId = VentaService.usuarioId(c, Sesion.actual().getUsuario());
                int proveedorId;
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM proveedores WHERE nombre = ? AND activo = 1")) {
                    ps.setString(1, proveedorFinal);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new NegocioException("El proveedor no está registrado o está desactivado");
                        proveedorId = rs.getInt(1);
                    }
                }
                int compraId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO compras (fecha, proveedor_id, usuario_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(momento));
                    ps.setInt(2, proveedorId);
                    ps.setInt(3, usuarioId);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); compraId = k.getInt(1); }
                }
                for (LineaDetalle l : lineas) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE productos SET cantidad = cantidad + ? WHERE codigo = ?")) {
                        ps.setInt(1, l.getCantidad());
                        ps.setString(2, l.getCodigo());
                        if (ps.executeUpdate() == 0) {
                            throw new NegocioException("El producto " + l.getCodigo() + " ya no existe");
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO compra_detalle (compra_id, producto_id, codigo, nombre, cantidad, precio_unitario) "
                                    + "VALUES (?, (SELECT id FROM productos WHERE codigo = ?), ?, ?, ?, ?)")) {
                        ps.setInt(1, compraId);
                        ps.setString(2, l.getCodigo());
                        ps.setString(3, l.getCodigo());
                        ps.setString(4, l.getNombre());
                        ps.setInt(5, l.getCantidad());
                        ps.setDouble(6, l.getPrecioUnitario());
                        ps.executeUpdate();
                    }
                }
                return compraId;
            });
        } catch (NegocioException e) {
            inventario.recargar();
            throw e;
        }

        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            if (p != null) p.setCantidad(p.getCantidad() + l.getCantidad());
        }
        return new Compra(id, momento, proveedorFinal, Sesion.actual().getNombre(), new ArrayList<>(lineas));
    }

    public List<Compra> listar() {
        return Db.consulta(c -> {
            Map<Integer, List<LineaDetalle>> detalle = new HashMap<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT compra_id, codigo, nombre, cantidad, precio_unitario FROM compra_detalle ORDER BY compra_id, id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalle.computeIfAbsent(rs.getInt(1), k -> new ArrayList<>())
                            .add(new LineaDetalle(rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5)));
                }
            }
            List<Compra> compras = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT c.id, c.fecha, p.nombre, u.nombre FROM compras c "
                            + "JOIN proveedores p ON p.id = c.proveedor_id "
                            + "JOIN usuarios u ON u.id = c.usuario_id ORDER BY c.id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    compras.add(new Compra(id, rs.getTimestamp(2).toLocalDateTime(), rs.getString(3), rs.getString(4),
                            detalle.getOrDefault(id, new ArrayList<>())));
                }
            }
            return compras;
        });
    }
}
