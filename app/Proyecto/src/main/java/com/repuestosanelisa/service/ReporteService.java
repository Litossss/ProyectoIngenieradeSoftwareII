package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Compra;
import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.model.TipoReporte;
import com.repuestosanelisa.model.Venta;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.util.Fmt;
import com.repuestosanelisa.util.NegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


public class ReporteService {

    private final Inventario inventario;
    private final VentaService ventas;
    private final CompraService compras;

    public ReporteService(Inventario inventario, VentaService ventas, CompraService compras) {
        this.inventario = inventario;
        this.ventas = ventas;
        this.compras = compras;
    }

    public String generar(TipoReporte tipo, LocalDate desde, LocalDate hasta, String categoria) {
        Permisos.exigir(Modulo.REPORTES);
        if (tipo != TipoReporte.INVENTARIO) {
            if (desde == null || hasta == null) throw new NegocioException("Indique el rango de fechas (desde y hasta)");
            if (desde.isAfter(hasta)) throw new NegocioException("La fecha 'Desde' no puede ser mayor que 'Hasta'");
        }
        boolean todas = categoria == null || categoria.isBlank() || categoria.equals(Inventario.TODAS);
        return switch (tipo) {
            case INVENTARIO -> inventario(todas, categoria);
            case VENTAS -> ventas(desde, hasta, todas, categoria);
            case COMPRAS -> compras(desde, hasta, todas, categoria);
        };
    }

    private String inventario(boolean todas, String categoria) {
        List<Producto> lista = inventario.getProductos().stream()
                .filter(p -> todas || p.getCategoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Producto::getCodigo, String.CASE_INSENSITIVE_ORDER)).toList();
        if (lista.isEmpty()) throw new NegocioException("No existen datos para el filtro seleccionado");

        StringBuilder sb = cabecera("Reporte de inventario", todas ? "Todas" : categoria);
        sb.append(String.format("%-8s %-28s %-12s %6s %12s %14s%n",
                "Código", "Producto", "Categoría", "Cant.", "Precio", "Valor"));
        sb.append("-".repeat(86)).append("\n");
        double total = 0;
        for (Producto p : lista) {
            double valor = p.getCantidad() * p.getPrecio();
            total += valor;
            sb.append(String.format("%-8s %-28s %-12s %6d %12s %14s%n", p.getCodigo(),
                    Fmt.cortar(p.getNombre(), 28), Fmt.cortar(p.getCategoria(), 12), p.getCantidad(),
                    Fmt.moneda(p.getPrecio()), Fmt.moneda(valor)));
        }
        sb.append("-".repeat(86)).append("\n");
        sb.append(String.format("Productos: %d    Valor total del inventario: %s%n", lista.size(), Fmt.moneda(total)));
        return sb.toString();
    }

    private String ventas(LocalDate desde, LocalDate hasta, boolean todas, String categoria) {
        StringBuilder filas = new StringBuilder();
        double total = 0;
        int n = 0;
        for (Venta v : ventas.listar()) {
            if (fueraDeRango(v.getFecha(), desde, hasta)) continue;
            for (LineaDetalle l : v.getLineas()) {
                if (!todas && !categoriaDe(l.getCodigo()).equalsIgnoreCase(categoria)) continue;
                filas.append(String.format("%-16s %-8s %-28s %5d %12s %12s%n", v.getFecha().format(Fmt.FECHA_HORA),
                        l.getCodigo(), Fmt.cortar(l.getNombre(), 28), l.getCantidad(),
                        Fmt.moneda(l.getPrecioUnitario()), Fmt.moneda(l.getSubtotal())));
                total += l.getSubtotal();
                n++;
            }
        }
        if (n == 0) throw new NegocioException("No existen datos para el rango seleccionado");

        StringBuilder sb = cabecera("Reporte de ventas (salidas) " + Fmt.FECHA.format(desde) + " - "
                + Fmt.FECHA.format(hasta), todas ? "Todas" : categoria);
        sb.append(String.format("%-16s %-8s %-28s %5s %12s %12s%n",
                "Fecha", "Código", "Producto", "Cant.", "Precio", "Subtotal"));
        sb.append("-".repeat(86)).append("\n").append(filas);
        sb.append("-".repeat(86)).append("\n");
        sb.append(String.format("Líneas: %d    Total general: %s%n", n, Fmt.moneda(total)));
        return sb.toString();
    }

    private String compras(LocalDate desde, LocalDate hasta, boolean todas, String categoria) {
        StringBuilder filas = new StringBuilder();
        double total = 0;
        int n = 0;
        for (Compra c : compras.listar()) {
            if (fueraDeRango(c.getFecha(), desde, hasta)) continue;
            for (LineaDetalle l : c.getLineas()) {
                if (!todas && !categoriaDe(l.getCodigo()).equalsIgnoreCase(categoria)) continue;
                filas.append(String.format("%-16s %-12s %-8s %-22s %5d %12s %12s%n",
                        c.getFecha().format(Fmt.FECHA_HORA), Fmt.cortar(c.getProveedor(), 12), l.getCodigo(),
                        Fmt.cortar(l.getNombre(), 22), l.getCantidad(),
                        Fmt.moneda(l.getPrecioUnitario()), Fmt.moneda(l.getSubtotal())));
                total += l.getSubtotal();
                n++;
            }
        }
        if (n == 0) throw new NegocioException("No existen datos para el rango seleccionado");

        StringBuilder sb = cabecera("Reporte de compras (entradas) " + Fmt.FECHA.format(desde) + " - "
                + Fmt.FECHA.format(hasta), todas ? "Todas" : categoria);
        sb.append(String.format("%-16s %-12s %-8s %-22s %5s %12s %12s%n",
                "Fecha", "Proveedor", "Código", "Producto", "Cant.", "Costo", "Subtotal"));
        sb.append("-".repeat(94)).append("\n").append(filas);
        sb.append("-".repeat(94)).append("\n");
        sb.append(String.format("Líneas: %d    Total general: %s%n", n, Fmt.moneda(total)));
        return sb.toString();
    }

    private StringBuilder cabecera(String titulo, String categoria) {
        StringBuilder sb = new StringBuilder();
        sb.append("REPUESTOS ANELISA\n").append(titulo).append("\n");
        sb.append("Categoría: ").append(categoria).append("    Generado: ")
                .append(LocalDateTime.now().format(Fmt.FECHA_HORA)).append("\n\n");
        return sb;
    }

    private boolean fueraDeRango(LocalDateTime f, LocalDate desde, LocalDate hasta) {
        LocalDate d = f.toLocalDate();
        return d.isBefore(desde) || d.isAfter(hasta);
    }

    private String categoriaDe(String codigo) {
        Producto p = inventario.buscarProducto(codigo);
        return p == null ? "" : p.getCategoria();
    }
}
