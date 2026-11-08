package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Compra;
import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.model.Venta;
import com.repuestosanelisa.util.Config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class DashboardService {

    public record Kpis(int productos, int pocoStock, double ventasHoy, double comprasMes) {}

    public record FilaVenta(LocalDateTime fecha, String producto, int cantidad, double total) {}

    private final Inventario inventario;
    private final VentaService ventas;
    private final CompraService compras;

    public DashboardService(Inventario inventario, VentaService ventas, CompraService compras) {
        this.inventario = inventario;
        this.ventas = ventas;
        this.compras = compras;
    }

    public Kpis kpis() {
        int poco = (int) inventario.getProductos().stream()
                .filter(p -> p.getCantidad() <= Config.UMBRAL_POCO_STOCK).count();
        LocalDate hoy = LocalDate.now();
        double ventasHoy = ventas.listar().stream()
                .filter(v -> v.getFecha().toLocalDate().equals(hoy)).mapToDouble(Venta::getTotal).sum();
        YearMonth mes = YearMonth.now();
        double comprasMes = compras.listar().stream()
                .filter(c -> YearMonth.from(c.getFecha()).equals(mes)).mapToDouble(Compra::getTotal).sum();
        return new Kpis(inventario.getProductos().size(), poco, ventasHoy, comprasMes);
    }

    public List<FilaVenta> ultimasVentas(int max) {
        List<Venta> vs = new ArrayList<>(ventas.listar());
        vs.sort(Comparator.comparing(Venta::getFecha).reversed());
        List<FilaVenta> filas = new ArrayList<>();
        for (Venta v : vs) {
            for (LineaDetalle l : v.getLineas()) {
                if (filas.size() >= max) return filas;
                filas.add(new FilaVenta(v.getFecha(), l.getNombre(), l.getCantidad(), l.getSubtotal()));
            }
        }
        return filas;
    }

    public List<Producto> pocoStock(int max) {
        return inventario.getProductos().stream()
                .filter(p -> p.getCantidad() <= Config.UMBRAL_POCO_STOCK)
                .sorted(Comparator.comparingInt(Producto::getCantidad)).limit(max).toList();
    }
}
