package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.service.DashboardService.FilaVenta;
import com.repuestosanelisa.util.Fmt;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;


public class InicioController {
    @FXML private Label lblProductos, lblPocoStock, lblVentasHoy, lblComprasMes;
    @FXML private VBox cardCompras, boxReporte;
    @FXML private TableView<FilaVenta> tablaVentas;
    @FXML private TableColumn<FilaVenta, String> colVFecha, colVProducto, colVCant, colVTotal;
    @FXML private TableView<Producto> tablaPoco;
    @FXML private TableColumn<Producto, String> colPCodigo, colPProducto, colPStock;

    @FXML
    private void initialize() {
        var ctx = Contexto.get();
        var k = ctx.dashboard.kpis();
        lblProductos.setText(String.valueOf(k.productos()));
        lblPocoStock.setText(String.valueOf(k.pocoStock()));
        lblVentasHoy.setText(Fmt.moneda(k.ventasHoy()));
        lblComprasMes.setText(Fmt.moneda(k.comprasMes()));

        boolean admin = Permisos.esAdmin();
        cardCompras.setVisible(admin);
        cardCompras.setManaged(admin);
        boxReporte.setVisible(admin);
        boxReporte.setManaged(admin);

        colVFecha.setCellValueFactory(c -> new SimpleStringProperty(Fmt.FECHA_CORTA.format(c.getValue().fecha())));
        colVProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().producto()));
        colVCant.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().cantidad())));
        colVTotal.setCellValueFactory(c -> new SimpleStringProperty(Fmt.moneda(c.getValue().total())));
        tablaVentas.setItems(FXCollections.observableArrayList(ctx.dashboard.ultimasVentas(8)));
        tablaVentas.setPlaceholder(new Label("Aún no hay ventas registradas"));

        colPCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colPProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colPStock.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        tablaPoco.setItems(FXCollections.observableArrayList(ctx.dashboard.pocoStock(8)));
        tablaPoco.setPlaceholder(new Label("Sin alertas de stock"));
    }

    @FXML private void nuevaVenta()          { Navegacion.ir(Modulo.VENTAS); }
    @FXML private void consultarInventario() { Navegacion.ir(Modulo.INVENTARIO); }
    @FXML private void generarReporte()      { Navegacion.ir(Modulo.REPORTES); }
}
