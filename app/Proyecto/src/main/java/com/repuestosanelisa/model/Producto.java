package com.repuestosanelisa.model;

public class Producto {
    private final String codigo;
    private String nombre;
    private String categoria;
    private int cantidad;
    private double precio;
    private String proveedor;
    private String descripcion;
    private String foto; // archivo dentro de app-data/fotos ("" si no tiene)

    public Producto(String codigo, String nombre, int cantidad, double precio) {
        this(codigo, nombre, "", cantidad, precio, "", "", "");
    }

    public Producto(String codigo, String nombre, String categoria, int cantidad, double precio,
                    String proveedor, String descripcion, String foto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precio = precio;
        this.proveedor = proveedor;
        this.descripcion = descripcion;
        this.foto = foto;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}
