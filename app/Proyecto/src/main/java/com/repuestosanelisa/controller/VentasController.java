package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.model.Venta;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.Comprobante;
import com.repuestosanelisa.util.Fmt;
import com.repuestosanelisa.util.NegocioException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.time.LocalDateTime;

public class VentasController {
    @FXML private TextField txtBuscar, txtCliente;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colPCodigo, colPProducto, colPStock;
    @FXML private TableColumn<Producto, Producto> colPAgregar;
    @FXML private Label lblUsuario, lblFecha, lblTotal, lblError;
    @FXML private TableView<LineaDetalle> tablaDetalle;
    @FXML private TableColumn<LineaDetalle, String> colDProducto, colDPrecio, colDSubtotal;
    @FXML private TableColumn<LineaDetalle, Integer> colDCant;
    @FXML private TableColumn<LineaDetalle, LineaDetalle> colDQuitar;

    private final ObservableList<LineaDetalle> detalle = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        lblUsuario.setText("Usuario: " + Sesion.actual().getNombre());
        lblFecha.setText("Fecha: " + Fmt.FECHA_HORA.format(LocalDateTime.now()));

        colPCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colPProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colPStock.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        colPAgregar.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colPAgregar.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("+ Agregar");
            { b.getStyleClass().add("btn-mini"); b.setOnAction(e -> agregar(getItem())); }
            @Override protected void updateItem(Producto p, boolean vacio) {
                super.updateItem(p, vacio);
                setGraphic(vacio || p == null ? null : b);
            }
        });
        tablaProductos.setPlaceholder(new Label("No se encontraron productos"));

        colDProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colDCant.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCantidad()));
        colDCant.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colDCant.setOnEditCommit(ev -> {
            Integer nuevo = ev.getNewValue();
            if (nuevo == null || nuevo <= 0) { Alertas.mostrarError(lblError, "La cantidad debe ser mayor a 0"); }
            else { Alertas.ocultarError(lblError); ev.getRowValue().setCantidad(nuevo); }
            tablaDetalle.refresh();
            actualizarTotal();
        });
        colDPrecio.setCellValueFactory(c -> new SimpleStringProperty(Fmt.moneda(c.getValue().getPrecioUnitario())));
        colDSubtotal.setCellValueFactory(c -> new SimpleStringProperty(Fmt.moneda(c.getValue().getSubtotal())));
        colDQuitar.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colDQuitar.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("✕");
            { b.getStyleClass().add("btn-quitar"); b.setOnAction(e -> { detalle.remove(getItem()); actualizarTotal(); }); }
            @Override protected void updateItem(LineaDetalle l, boolean vacio) {
                super.updateItem(l, vacio);
                setGraphic(vacio || l == null ? null : b);
            }
        });
        tablaDetalle.setItems(detalle);
        tablaDetalle.setPlaceholder(new Label("Agregue productos desde la lista de la izquierda"));
        buscar();
        actualizarTotal();
    }

    @FXML
    private void buscar() {
        tablaProductos.setItems(FXCollections.observableArrayList(
                Contexto.get().inventario.buscar(txtBuscar.getText(), null)));
    }

    private void agregar(Producto p) {
        Alertas.ocultarError(lblError);
        if (p.getCantidad() <= 0) { Alertas.mostrarError(lblError, "Sin stock disponible de \"" + p.getNombre() + "\""); return; }
        for (LineaDetalle l : detalle) {
            if (l.getCodigo().equals(p.getCodigo())) {
                if (l.getCantidad() + 1 > p.getCantidad()) {
                    Alertas.mostrarError(lblError, "Stock insuficiente de \"" + p.getNombre() + "\" (disponible: " + p.getCantidad() + ")");
                    return;
                }
                l.setCantidad(l.getCantidad() + 1);
                tablaDetalle.refresh();
                actualizarTotal();
                return;
            }
        }
        detalle.add(new LineaDetalle(p.getCodigo(), p.getNombre(), 1, p.getPrecio()));
        actualizarTotal();
    }

    private void actualizarTotal() {
        lblTotal.setText(Fmt.moneda(detalle.stream().mapToDouble(LineaDetalle::getSubtotal).sum()));
    }

    @FXML
    private void cancelar() {
        detalle.clear();
        txtCliente.clear();
        Alertas.ocultarError(lblError);
        actualizarTotal();
    }

    @FXML
    private void registrar() {
        Alertas.ocultarError(lblError);
        try {
            Venta v = Contexto.get().ventas.registrar(txtCliente.getText(), new java.util.ArrayList<>(detalle));
            detalle.clear();
            txtCliente.clear();
            actualizarTotal();
            buscar();
            Alertas.comprobante(txtBuscar.getScene().getWindow(), "Comprobante de venta N° " + v.getId(),
                    Comprobante.venta(v), "venta-" + v.getId() + ".pdf");
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
            buscar();
        }
    }
}
