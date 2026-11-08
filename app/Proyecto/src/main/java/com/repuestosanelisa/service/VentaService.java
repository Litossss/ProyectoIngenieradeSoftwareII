package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.model.Venta;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class VentaService {

    private final Path archivo = Config.DATA_DIR.resolve("ventas.xml");
    private final List<Venta> ventas = new ArrayList<>();
    private final Inventario inventario;

    public VentaService(Inventario inventario) {
        this.inventario = inventario;
        if (Files.exists(archivo)) cargar();
        else guardar();
    }

    public List<Venta> listar() { return new ArrayList<>(ventas); }

    public Venta registrar(String cliente, List<LineaDetalle> lineas) {
        Permisos.exigir(Modulo.VENTAS);
        if (lineas.isEmpty()) throw new NegocioException("Agregue al menos un producto a la venta");

        // Primero se valida todo; solo después se descuenta (la venta es todo o nada).
        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            if (p == null) throw new NegocioException("El producto " + l.getCodigo() + " ya no existe");
            if (l.getCantidad() <= 0) throw new NegocioException("Ingrese una cantidad válida");
            if (l.getCantidad() > p.getCantidad()) {
                throw new NegocioException("Stock insuficiente de " + p.getNombre()
                        + ": solo hay " + p.getCantidad() + " unidades");
            }
        }
        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            p.setCantidad(p.getCantidad() - l.getCantidad());
        }
        try {
            inventario.persistir();
        } catch (NegocioException e) {
            devolverStock(lineas);
            throw e;
        }

        Venta v = new Venta(ventas.stream().mapToInt(Venta::getId).max().orElse(0) + 1,
                LocalDateTime.now(), Sesion.actual().getNombre(),
                cliente == null ? "" : cliente.trim(), new ArrayList<>(lineas));
        ventas.add(v);
        try {
            guardar();
        } catch (NegocioException e) {
            ventas.remove(v);
            devolverStock(lineas);
            try { inventario.persistir(); } catch (NegocioException ignorada) { /* se informa el error original */ }
            throw e;
        }
        return v;
    }

    private void devolverStock(List<LineaDetalle> lineas) {
        for (LineaDetalle l : lineas) {
            Producto p = inventario.buscarProducto(l.getCodigo());
            if (p != null) p.setCantidad(p.getCantidad() + l.getCantidad());
        }
    }

    private void cargar() {
        // <AQUÍ VA MYSQL> — SELECT ventas y su detalle
        try {
            Document d = XmlUtil.leer(archivo);
            for (Element e : XmlUtil.hijos(d.getDocumentElement(), "venta")) {
                ventas.add(new Venta(Integer.parseInt(XmlUtil.texto(e, "id")),
                        LocalDateTime.parse(XmlUtil.texto(e, "fecha")), XmlUtil.texto(e, "usuario"),
                        XmlUtil.texto(e, "cliente"), XmlUtil.leerLineas(e)));
            }
        } catch (Exception ex) {
            throw new NegocioException("No se pudo leer ventas.xml: " + ex.getMessage());
        }
    }

    private void guardar() {
        // <AQUÍ VA MYSQL> — INSERT INTO ventas / detalle_venta
        try {
            Document d = XmlUtil.nuevo("ventas");
            for (Venta v : ventas) {
                Element e = XmlUtil.agregar(d, d.getDocumentElement(), "venta", null);
                XmlUtil.agregar(d, e, "id", String.valueOf(v.getId()));
                XmlUtil.agregar(d, e, "fecha", v.getFecha().toString());
                XmlUtil.agregar(d, e, "usuario", v.getUsuario());
                XmlUtil.agregar(d, e, "cliente", v.getCliente());
                XmlUtil.escribirLineas(d, e, v.getLineas());
            }
            XmlUtil.escribir(d, archivo);
        } catch (Exception ex) {
            throw new NegocioException("No se pudo guardar ventas.xml: " + ex.getMessage());
        }
    }
}
