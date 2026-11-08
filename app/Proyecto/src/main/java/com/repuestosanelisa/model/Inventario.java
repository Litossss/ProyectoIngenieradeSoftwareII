package com.repuestosanelisa.model;

import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.service.InventarioService;
import com.repuestosanelisa.util.NegocioException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/** Existencias del almacén: lista en memoria + persistencia en MySQL mediante InventarioService. */
public class Inventario {

    public static final String TODAS = "Todas";

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final InventarioService servicio;

    public Inventario() {
        servicio = new InventarioService();
        recargar();
    }

    /** Vuelve a leer los productos desde MySQL (por si el stock cambió fuera de esta ventana). */
    public void recargar() {
        productos.setAll(servicio.cargarProductos());
    }

    public ObservableList<Producto> getProductos() {
        return FXCollections.unmodifiableObservableList(productos);
    }

    /** RF05: busca por nombre, código o categoría y opcionalmente filtra por categoría. */
    public List<Producto> buscar(String texto, String categoria) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        boolean todas = categoria == null || categoria.isBlank() || categoria.equals(TODAS);
        return productos.stream()
                .filter(p -> t.isEmpty() || p.getCodigo().toLowerCase().contains(t)
                        || p.getNombre().toLowerCase().contains(t)
                        || p.getCategoria().toLowerCase().contains(t))
                .filter(p -> todas || p.getCategoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Producto::getCodigo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<String> categorias() {
        TreeSet<String> s = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Producto p : productos) if (!p.getCategoria().isBlank()) s.add(p.getCategoria());
        return List.copyOf(s);
    }

    public Producto buscarProducto(String codigo) {
        for (Producto p : productos) if (p.getCodigo().equalsIgnoreCase(codigo)) return p;
        return null;
    }

    public void agregarProducto(Producto producto) {
        Permisos.exigir(Modulo.INVENTARIO);
        validar(producto);
        if (buscarProducto(producto.getCodigo()) != null) {
            throw new NegocioException("El código '" + producto.getCodigo() + "' ya existe");
        }
        servicio.insertar(producto);          // primero MySQL; si falla, la lista no cambia
        productos.add(producto);
    }

    /** El código no cambia. La foto solo se reemplaza si viene una nueva. */
    public void actualizarProducto(Producto datos) {
        Permisos.exigir(Modulo.INVENTARIO);
        validar(datos);
        Producto p = buscarProducto(datos.getCodigo());
        if (p == null) throw new NegocioException("El producto no existe en el inventario");

        String foto = datos.getFoto().isBlank() ? p.getFoto() : datos.getFoto();
        Producto nuevo = new Producto(p.getCodigo(), datos.getNombre(), datos.getCategoria(), datos.getCantidad(),
                datos.getPrecio(), datos.getProveedor(), datos.getDescripcion(), foto);
        servicio.actualizar(nuevo);           // primero MySQL; si falla, el producto no cambia
        p.setNombre(nuevo.getNombre());
        p.setCategoria(nuevo.getCategoria());
        p.setCantidad(nuevo.getCantidad());
        p.setPrecio(nuevo.getPrecio());
        p.setProveedor(nuevo.getProveedor());
        p.setDescripcion(nuevo.getDescripcion());
        p.setFoto(nuevo.getFoto());
    }

    /** Solo el Administrador elimina productos. */
    public void eliminarProducto(Producto producto) {
        Permisos.exigirAdmin();
        if (producto == null || !productos.contains(producto)) return;
        servicio.eliminar(producto.getCodigo());
        productos.remove(producto);
    }

    private void validar(Producto p) {
        if (p.getCodigo() == null || p.getCodigo().isBlank() || p.getNombre() == null || p.getNombre().isBlank()
                || p.getCategoria() == null || p.getCategoria().isBlank()) {
            throw new NegocioException("Faltan datos obligatorios (código, nombre y categoría)");
        }
        if (p.getCantidad() < 0 || p.getPrecio() < 0) {
            throw new NegocioException("Cantidad y precio no pueden ser negativos");
        }
    }
}
