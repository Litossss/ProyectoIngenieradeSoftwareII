package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Compra;
import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.util.Config;
import com.repuestosanelisa.util.NegocioException;
import com.repuestosanelisa.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class CompraService {

    private final Path archivo = Config.DATA_DIR.resolve("compras.xml");
    private final List<Compra> compras = new ArrayList<>();
    private final Inventario inventario;
    private final ProveedorService proveedores;

    public CompraService(Inventario inventario, ProveedorService proveedores) {
        this.inventario = inventario;
        this.proveedores = proveedores;
        if (Files.exists(archivo)) cargar();
        else guardar();
    }

    public List<Compra> listar() { return new ArrayList<>(compras); }

    public Compra registrar(String proveedor, LocalDate fecha, List<LineaDetalle> lineas) {
        Permisos.exigir(Modulo.COMPRAS);
        if (proveedor == null || proveedor.isBlank()) throw new NegocioException("Indique el proveedor");
        if (!proveedores.existe(proveedor.trim())) throw new NegocioException("El proveedor no está registrado");
        if (lineas.isEmpty()) throw new NegocioException("Agregue al menos un producto a la compra");
        for (LineaDetalle l : lineas) {
            if (inventario.buscarProducto(l.getCodigo()) == null) {
                throw new NegocioException("El producto " + l.getCodigo() + " no está registrado");
            }
            if (l.getCantidad() <= 0) throw new NegocioException("Ingrese una cantidad válida");
        }

        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            p.setCantidad(p.getCantidad() + l.getCantidad());
        }
        try {
            inventario.persistir();
        } catch (NegocioException e) {
            quitarStock(lineas);
            throw e;
        }

        LocalDate f = fecha == null ? LocalDate.now() : fecha;
        Compra c = new Compra(compras.stream().mapToInt(Compra::getId).max().orElse(0) + 1,
                LocalDateTime.of(f, LocalTime.now()), proveedor.trim(), Sesion.actual().getNombre(),
                new ArrayList<>(lineas));
        compras.add(c);
        try {
            guardar();
        } catch (NegocioException e) {
            compras.remove(c);
            quitarStock(lineas);
            try { inventario.persistir(); } catch (NegocioException ignorada) { /* se informa el error original */ }
            throw e;
        }
        return c;
    }

    private void quitarStock(List<LineaDetalle> lineas) {
        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            if (p != null) p.setCantidad(p.getCantidad() - l.getCantidad());
        }
    }

    private void cargar() {
        // <AQUÍ VA MYSQL> — SELECT compras y su detalle
        try {
            Document d = XmlUtil.leer(archivo);
            for (Element e : XmlUtil.hijos(d.getDocumentElement(), "compra")) {
                compras.add(new Compra(Integer.parseInt(XmlUtil.texto(e, "id")),
                        LocalDateTime.parse(XmlUtil.texto(e, "fecha")), XmlUtil.texto(e, "proveedor"),
                        XmlUtil.texto(e, "usuario"), XmlUtil.leerLineas(e)));
            }
        } catch (Exception ex) {
            throw new NegocioException("No se pudo leer compras.xml: " + ex.getMessage());
        }
    }

    private void guardar() {
        // <AQUÍ VA MYSQL> — INSERT INTO compras / detalle_compra
        try {
            Document d = XmlUtil.nuevo("compras");
            for (Compra c : compras) {
                Element e = XmlUtil.agregar(d, d.getDocumentElement(), "compra", null);
                XmlUtil.agregar(d, e, "id", String.valueOf(c.getId()));
                XmlUtil.agregar(d, e, "fecha", c.getFecha().toString());
                XmlUtil.agregar(d, e, "proveedor", c.getProveedor());
                XmlUtil.agregar(d, e, "usuario", c.getUsuario());
                XmlUtil.escribirLineas(d, e, c.getLineas());
            }
            XmlUtil.escribir(d, archivo);
        } catch (Exception ex) {
            throw new NegocioException("No se pudo guardar compras.xml: " + ex.getMessage());
        }
    }
}
