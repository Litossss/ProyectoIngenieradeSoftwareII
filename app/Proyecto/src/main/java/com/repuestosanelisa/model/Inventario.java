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


public class Inventario {

    public static final String TODAS = "Todas";

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final InventarioService servicio;

    public Inventario() {
        servicio = new InventarioService();
        try {
            productos.addAll(servicio.cargarProductos());
        } catch (Exception e) {
            throw new NegocioException("No se pudo cargar el inventario desde XML: " + e.getMessage());
        }
    }

    public ObservableList<Producto> getProductos() {
        return FXCollections.unmodifiableObservableList(productos);
    }


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
        productos.add(producto);
        try {
            persistir();
        } catch (NegocioException e) {
            productos.remove(producto);
            throw e;
        }
    }


    public void actualizarProducto(Producto datos) {
        Permisos.exigir(Modulo.INVENTARIO);
        validar(datos);
        Producto p = buscarProducto(datos.getCodigo());
        if (p == null) throw new NegocioException("El producto no existe en el inventario");

        Producto copia = new Producto(p.getCodigo(), p.getNombre(), p.getCategoria(), p.getCantidad(),
                p.getPrecio(), p.getProveedor(), p.getDescripcion(), p.getFoto());
        p.setNombre(datos.getNombre());
        p.setCategoria(datos.getCategoria());
        p.setCantidad(datos.getCantidad());
        p.setPrecio(datos.getPrecio());
        p.setProveedor(datos.getProveedor());
        p.setDescripcion(datos.getDescripcion());
        if (!datos.getFoto().isBlank()) p.setFoto(datos.getFoto());
        try {
            persistir();
        } catch (NegocioException e) {
            p.setNombre(copia.getNombre());
            p.setCategoria(copia.getCategoria());
            p.setCantidad(copia.getCantidad());
            p.setPrecio(copia.getPrecio());
            p.setProveedor(copia.getProveedor());
            p.setDescripcion(copia.getDescripcion());
            p.setFoto(copia.getFoto());
            throw e;
        }
    }


    public void eliminarProducto(Producto producto) {
        Permisos.exigirAdmin();
        if (producto == null || !productos.contains(producto)) return;
        int indice = productos.indexOf(producto);
        productos.remove(producto);
        try {
            persistir();
        } catch (NegocioException e) {
            productos.add(indice, producto);
            throw e;
        }
    }


    public void persistir() {
        try {
            servicio.guardarProductos(List.copyOf(productos));
        } catch (Exception e) {
            throw new NegocioException("No se pudo guardar el inventario en XML: " + e.getMessage());
        }
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
