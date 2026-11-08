package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Proveedor;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.util.Config;
import com.repuestosanelisa.util.NegocioException;
import com.repuestosanelisa.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class ProveedorService {

    private final Path archivo = Config.DATA_DIR.resolve("proveedores.xml");
    private final List<Proveedor> proveedores = new ArrayList<>();

    public ProveedorService() {
        if (Files.exists(archivo)) {
            cargar();
        } else if (Config.CARGAR_DATOS_DEMO) {
            proveedores.addAll(DatosDemo.proveedores());
            guardar();
        }
    }

    public List<String> nombres() {
        return proveedores.stream().map(Proveedor::getNombre).sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public boolean existe(String nombre) {
        return proveedores.stream().anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
    }

    public void registrar(String nombre, String contacto) {
        Permisos.exigir(Modulo.COMPRAS);
        if (nombre == null || nombre.isBlank()) throw new NegocioException("Ingrese el nombre del proveedor");
        if (existe(nombre.trim())) throw new NegocioException("El proveedor ya está registrado");
        Proveedor nuevo = new Proveedor(nombre.trim(), contacto == null ? "" : contacto.trim());
        proveedores.add(nuevo);
        try {
            guardar();
        } catch (NegocioException e) {
            proveedores.remove(nuevo);
            throw e;
        }
    }

    private void cargar() {
        // <AQUÍ VA MYSQL> — SELECT nombre, contacto FROM proveedores
        try {
            Document d = XmlUtil.leer(archivo);
            for (Element e : XmlUtil.hijos(d.getDocumentElement(), "proveedor")) {
                proveedores.add(new Proveedor(XmlUtil.texto(e, "nombre"), XmlUtil.texto(e, "contacto")));
            }
        } catch (Exception ex) {
            throw new NegocioException("No se pudo leer proveedores.xml: " + ex.getMessage());
        }
    }

    private void guardar() {
        // <AQUÍ VA MYSQL> — INSERT INTO proveedores (nombre, contacto) VALUES (?, ?)
        try {
            Document d = XmlUtil.nuevo("proveedores");
            for (Proveedor p : proveedores) {
                Element e = XmlUtil.agregar(d, d.getDocumentElement(), "proveedor", null);
                XmlUtil.agregar(d, e, "nombre", p.getNombre());
                XmlUtil.agregar(d, e, "contacto", p.getContacto());
            }
            XmlUtil.escribir(d, archivo);
        } catch (Exception ex) {
            throw new NegocioException("No se pudo guardar proveedores.xml: " + ex.getMessage());
        }
    }
}
