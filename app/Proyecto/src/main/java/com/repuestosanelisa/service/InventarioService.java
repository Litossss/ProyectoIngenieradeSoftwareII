
package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Producto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InventarioService {

    private final Path archivo =
            Path.of("app-data", "productos.xml");

    public List<Producto> cargarProductos() throws Exception {
        List<Producto> productos = new ArrayList<>();

        if (Files.notExists(archivo)) {
            return productos;
        }

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document documento = builder.parse(archivo.toFile());
        documento.getDocumentElement().normalize();

        NodeList nodos =
                documento.getElementsByTagName("producto");

        for (int i = 0; i < nodos.getLength(); i++) {
            Element elemento = (Element) nodos.item(i);

            String codigo = leer(elemento, "codigo");
            String nombre = leer(elemento, "nombre");
            int cantidad = Integer.parseInt(
                    leer(elemento, "cantidad"));
            double precio = Double.parseDouble(
                    leer(elemento, "precio"));

            if (cantidad < 0 || precio < 0) {
                throw new IOException(
                        "Hay cantidades o precios negativos en el XML.");
            }

            productos.add(new Producto(
                    codigo, nombre, cantidad, precio));
        }

        return productos;
    }

    public void guardarProductos(List<Producto> productos)
            throws Exception {

        Path carpeta = archivo.getParent();
        Files.createDirectories(carpeta);

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document documento = builder.newDocument();

        Element raiz = documento.createElement("inventario");
        documento.appendChild(raiz);

        for (Producto producto : productos) {
            Element elemento =
                    documento.createElement("producto");

            agregarElemento(documento, elemento,
                    "codigo", producto.getCodigo());
            agregarElemento(documento, elemento,
                    "nombre", producto.getNombre());
            agregarElemento(documento, elemento,
                    "cantidad", String.valueOf(
                            producto.getCantidad()));
            agregarElemento(documento, elemento,
                    "precio", String.valueOf(
                            producto.getPrecio()));

            raiz.appendChild(elemento);
        }

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();

        transformerFactory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer =
                transformerFactory.newTransformer();

        transformer.setOutputProperty(
                OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                OutputKeys.ENCODING, "UTF-8");

        transformer.transform(
                new DOMSource(documento),
                new StreamResult(archivo.toFile()));
    }

    private String leer(Element padre, String etiqueta)
            throws IOException {

        NodeList nodos = padre.getElementsByTagName(etiqueta);

        if (nodos.getLength() == 0) {
            throw new IOException(
                    "Falta el campo " + etiqueta + " en el XML.");
        }

        return nodos.item(0).getTextContent();
    }

    private void agregarElemento(
            Document documento,
            Element padre,
            String etiqueta,
            String valor) {

        Element elemento = documento.createElement(etiqueta);
        elemento.setTextContent(valor);
        padre.appendChild(elemento);
    }
}
