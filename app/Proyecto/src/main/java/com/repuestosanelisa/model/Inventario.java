
package com.repuestosanelisa.model;

import com.repuestosanelisa.service.InventarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;

public class Inventario {

    private final ObservableList<Producto> productos;
    private final InventarioService servicio;

    public Inventario() throws IOException {
        servicio = new InventarioService();
        productos = FXCollections.observableArrayList();

        try {
            productos.addAll(servicio.cargarProductos());
        } catch (Exception e) {
            throw new IOException(
                    "No se pudo cargar el inventario desde XML.", e);
        }
    }

    public ObservableList<Producto> getProductos() {
        return FXCollections.unmodifiableObservableList(productos);
    }

    public void agregarProducto(Producto producto)
            throws IOException {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no puede ser nulo.");
        }

        if (buscarProducto(producto.getCodigo()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese código.");
        }

        productos.add(producto);
        guardar();
    }

    public Producto buscarProducto(String codigo) {
        for (Producto producto : productos) {
            if (producto.getCodigo().equalsIgnoreCase(codigo)) {
                return producto;
            }
        }

        return null;
    }

    public void actualizarProducto(
            Producto producto,
            String nombre,
            int cantidad,
            double precio) throws IOException {

        if (producto == null || !productos.contains(producto)) {
            throw new IllegalArgumentException(
                    "El producto no existe en el inventario.");
        }

        if (nombre == null || nombre.isBlank()
                || cantidad < 0 || precio < 0) {
            throw new IllegalArgumentException(
                    "Los datos del producto no son válidos.");
        }

        String nombreAnterior = producto.getNombre();
        int cantidadAnterior = producto.getCantidad();
        double precioAnterior = producto.getPrecio();

        producto.setNombre(nombre.trim());
        producto.setCantidad(cantidad);
        producto.setPrecio(precio);

        try {
            guardar();
        } catch (IOException e) {
            producto.setNombre(nombreAnterior);
            producto.setCantidad(cantidadAnterior);
            producto.setPrecio(precioAnterior);
            throw e;
        }
    }

    public void eliminarProducto(Producto producto)
            throws IOException {

        if (producto == null || !productos.contains(producto)) {
            return;
        }

        int indice = productos.indexOf(producto);
        productos.remove(producto);

        try {
            guardar();
        } catch (IOException e) {
            productos.add(indice, producto);
            throw e;
        }
    }

    private void guardar() throws IOException {
        try {
            List<Producto> copia = List.copyOf(productos);
            servicio.guardarProductos(copia);
        } catch (Exception e) {
            throw new IOException(
                    "No se pudo guardar el inventario en XML.", e);
        }
    }
}
