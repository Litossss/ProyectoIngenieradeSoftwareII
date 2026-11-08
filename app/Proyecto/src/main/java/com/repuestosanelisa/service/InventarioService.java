package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.util.Config;
import com.repuestosanelisa.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class InventarioService {

    private final Path archivo = Config.DATA_DIR.resolve("productos.xml");

    public List<Producto> cargarProductos() throws Exception {
        List<Producto> productos = new ArrayList<>();

        // <AQUÍ VA MYSQL> — SELECT codigo, nombre, categoria, cantidad, precio, proveedor, descripcion, foto FROM productos
        if (Files.notExists(archivo)) {
            if (Config.CARGAR_DATOS_DEMO) productos.addAll(DatosDemo.productos());
            return productos;
        }

        Document documento = XmlUtil.leer(archivo);
        for (Element e : XmlUtil.hijos(documento.getDocumentElement(), "producto")) {
            int cantidad = Integer.parseInt(XmlUtil.texto(e, "cantidad"));
            double precio = Double.parseDouble(XmlUtil.texto(e, "precio"));
            if (cantidad < 0 || precio < 0) throw new IOException("Hay cantidades o precios negativos en el XML.");
            // Categoría, proveedor, descripción y foto son opcionales: el XML de la v0.1 sigue cargando.
            productos.add(new Producto(XmlUtil.texto(e, "codigo"), XmlUtil.texto(e, "nombre"),
                    XmlUtil.texto(e, "categoria"), cantidad, precio, XmlUtil.texto(e, "proveedor"),
                    XmlUtil.texto(e, "descripcion"), XmlUtil.texto(e, "foto")));
        }
        return productos;
    }

    public void guardarProductos(List<Producto> productos) throws Exception {
        // <AQUÍ VA MYSQL> — INSERT / UPDATE / DELETE de productos dentro de una transacción
        Document documento = XmlUtil.nuevo("inventario");
        Element raiz = documento.getDocumentElement();
        for (Producto p : productos) {
            Element e = XmlUtil.agregar(documento, raiz, "producto", null);
            XmlUtil.agregar(documento, e, "codigo", p.getCodigo());
            XmlUtil.agregar(documento, e, "nombre", p.getNombre());
            XmlUtil.agregar(documento, e, "categoria", p.getCategoria());
            XmlUtil.agregar(documento, e, "cantidad", String.valueOf(p.getCantidad()));
            XmlUtil.agregar(documento, e, "precio", String.valueOf(p.getPrecio()));
            XmlUtil.agregar(documento, e, "proveedor", p.getProveedor());
            XmlUtil.agregar(documento, e, "descripcion", p.getDescripcion());
            XmlUtil.agregar(documento, e, "foto", p.getFoto());
        }
        XmlUtil.escribir(documento, archivo);
    }
}
