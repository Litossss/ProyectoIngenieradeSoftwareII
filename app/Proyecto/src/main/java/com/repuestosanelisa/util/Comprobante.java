package com.repuestosanelisa.util;

import com.repuestosanelisa.model.Compra;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Venta;

import java.util.List;


public final class Comprobante {
    private Comprobante() {}

    public static String venta(Venta v) {
        StringBuilder sb = new StringBuilder();
        sb.append("REPUESTOS ANELISA\n");
        sb.append(String.format("COMPROBANTE DE VENTA N° %06d%n", v.getId()));
        sb.append("Fecha: ").append(v.getFecha().format(Fmt.FECHA_HORA)).append("\n");
        sb.append("Registrado por: ").append(v.getUsuario()).append("\n");
        if (v.getCliente() != null && !v.getCliente().isBlank()) sb.append("Cliente: ").append(v.getCliente()).append("\n");
        sb.append("\n");
        detalle(sb, v.getLineas(), "Precio");
        sb.append(String.format("%nTOTAL: %s%n", Fmt.moneda(v.getTotal())));
        return sb.toString();
    }

    public static String compra(Compra c) {
        StringBuilder sb = new StringBuilder();
        sb.append("REPUESTOS ANELISA\n");
        sb.append(String.format("COMPROBANTE DE COMPRA N° %06d%n", c.getId()));
        sb.append("Fecha: ").append(c.getFecha().format(Fmt.FECHA_HORA)).append("\n");
        sb.append("Proveedor: ").append(c.getProveedor()).append("\n");
        sb.append("Registrado por: ").append(c.getUsuario()).append("\n\n");
        detalle(sb, c.getLineas(), "Costo");
        sb.append(String.format("%nTOTAL COMPRA: %s%n", Fmt.moneda(c.getTotal())));
        return sb.toString();
    }

    private static void detalle(StringBuilder sb, List<LineaDetalle> lineas, String colPrecio) {
        sb.append(String.format("%-8s %-26s %5s %12s %12s%n", "Código", "Producto", "Cant.", colPrecio, "Subtotal"));
        sb.append("-".repeat(68)).append("\n");
        for (LineaDetalle l : lineas) {
            sb.append(String.format("%-8s %-26s %5d %12s %12s%n", l.getCodigo(), Fmt.cortar(l.getNombre(), 26),
                    l.getCantidad(), Fmt.moneda(l.getPrecioUnitario()), Fmt.moneda(l.getSubtotal())));
        }
    }
}
