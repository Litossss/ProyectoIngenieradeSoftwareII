package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.model.Proveedor;

import java.util.List;


final class DatosDemo {
    private DatosDemo() {}

    static List<Proveedor> proveedores() {
        return List.of(new Proveedor("Proveedor 1", ""), new Proveedor("Proveedor 2", ""),
                new Proveedor("Proveedor 3", ""), new Proveedor("Proveedor 4", ""));
    }

    static List<Producto> productos() {
        return List.of(
                new Producto("A-001", "Pastillas de freno delanteras", "Frenos", 12, 45000, "Proveedor 1", "", ""),
                new Producto("A-002", "Disco de freno", "Frenos", 8, 120000, "Proveedor 2", "", ""),
                new Producto("B-010", "Batería 12V", "Eléctrico", 25, 280000, "Proveedor 1", "", ""),
                new Producto("B-021", "Amortiguador trasero", "Suspensión", 3, 190000, "Proveedor 3", "", ""),
                new Producto("C-005", "Filtro de aceite", "Filtros", 40, 18000, "Proveedor 2", "", ""),
                new Producto("C-007", "Filtro de aire", "Filtros", 0, 25000, "Proveedor 2", "", ""),
                new Producto("C-033", "Espejo retrovisor", "Accesorios", 6, 60000, "Proveedor 4", "", ""),
                new Producto("D-001", "Bujía", "Motor", 50, 12000, "Proveedor 1", "", ""),
                new Producto("D-014", "Correa de distribución", "Motor", 2, 95000, "Proveedor 3", "", ""),
                new Producto("E-002", "Bombillo H4", "Eléctrico", 30, 9000, "Proveedor 4", "", ""),
                new Producto("E-010", "Cable de bujía", "Eléctrico", 15, 22000, "Proveedor 1", "", ""),
                new Producto("F-003", "Aceite 20W-50 (litro)", "Lubricantes", 35, 28000, "Proveedor 2", "", ""));
    }
}
