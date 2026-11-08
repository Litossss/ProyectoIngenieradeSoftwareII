package com.repuestosanelisa.util;

import com.repuestosanelisa.model.LineaDetalle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


public final class XmlUtil {
    private XmlUtil() {}

    public static Document leer(Path ruta) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document d = f.newDocumentBuilder().parse(ruta.toFile());
        d.getDocumentElement().normalize();
        return d;
    }

    public static Document nuevo(String raiz) throws Exception {
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        d.appendChild(d.createElement(raiz));
        return d;
    }

    public static void escribir(Document doc, Path ruta) throws Exception {
        if (ruta.getParent() != null) Files.createDirectories(ruta.getParent());
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        // Primero a un temporal, para no dejar el XML a medias si algo falla.
        Path tmp = ruta.resolveSibling(ruta.getFileName() + ".tmp");
        t.transform(new DOMSource(doc), new StreamResult(tmp.toFile()));
        Files.move(tmp, ruta, StandardCopyOption.REPLACE_EXISTING);
    }

    public static Element agregar(Document d, Element padre, String nombre, String valor) {
        Element e = d.createElement(nombre);
        if (valor != null) e.setTextContent(valor);
        padre.appendChild(e);
        return e;
    }

    public static List<Element> hijos(Element padre, String nombre) {
        List<Element> r = new ArrayList<>();
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element el && el.getTagName().equals(nombre)) r.add(el);
        }
        return r;
    }


    public static String texto(Element padre, String nombre) {
        List<Element> l = hijos(padre, nombre);
        return l.isEmpty() ? "" : l.get(0).getTextContent();
    }

    public static void escribirLineas(Document d, Element padre, List<LineaDetalle> lineas) {
        Element ls = agregar(d, padre, "lineas", null);
        for (LineaDetalle l : lineas) {
            Element e = agregar(d, ls, "linea", null);
            agregar(d, e, "codigo", l.getCodigo());
            agregar(d, e, "nombre", l.getNombre());
            agregar(d, e, "cantidad", String.valueOf(l.getCantidad()));
            agregar(d, e, "precio", String.valueOf(l.getPrecioUnitario()));
        }
    }

    public static List<LineaDetalle> leerLineas(Element padre) {
        List<LineaDetalle> r = new ArrayList<>();
        for (Element ls : hijos(padre, "lineas")) {
            for (Element e : hijos(ls, "linea")) {
                r.add(new LineaDetalle(texto(e, "codigo"), texto(e, "nombre"),
                        Integer.parseInt(texto(e, "cantidad")), Double.parseDouble(texto(e, "precio"))));
            }
        }
        return r;
    }
}
