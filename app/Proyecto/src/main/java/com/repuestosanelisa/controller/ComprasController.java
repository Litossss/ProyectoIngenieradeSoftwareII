package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Compra;
import com.repuestosanelisa.model.LineaDetalle;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.Comprobante;
import com.repuestosanelisa.util.Fmt;
import com.repuestosanelisa.util.NegocioException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

/** Registro de compras a proveedor Solo administrador. */
public class ComprasController {
    @FXML private ComboBox<String> cmbProveedor;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<LineaDetalle> tabla;
    @FXML private TableColumn<LineaDetalle, String> colCodigo, colProducto, colCosto, colSubtotal;
    @FXML private TableColumn<LineaDetalle, Integer> colCantidad;
    @FXML private TableColumn<LineaDetalle, LineaDetalle> colQuitar;
    @FXML private Label lblTotal, lblError;

    private final ObservableList<LineaDetalle> lineas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        dpFecha.setConverter(Fmt.convertidorFecha());
        dpFecha.setValue(LocalDate.now());
        cmbProveedor.getItems().setAll(Contexto.get().proveedores.nombres());

        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colProducto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colCantidad.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCantidad()));
        colCantidad.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colCantidad.setOnEditCommit(ev -> {
            Integer n = ev.getNewValue();
            if (n == null || n <= 0) Alertas.mostrarError(lblError, "La cantidad debe ser mayor a 0");
            else { Alertas.ocultarError(lblError); ev.getRowValue().setCantidad(n); }
            tabla.refresh();
            total();
        });
        colCosto.setCellValueFactory(c -> new SimpleStringProperty(Fmt.moneda(c.getValue().getPrecioUnitario())));
        colSubtotal.setCellValueFactory(c -> new SimpleStringProperty(Fmt.moneda(c.getValue().getSubtotal())));
        colQuitar.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colQuitar.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("Quitar");
            { b.getStyleClass().add("btn-mini"); b.setOnAction(e -> { lineas.remove(getItem()); total(); }); }
            @Override protected void updateItem(LineaDetalle l, boolean vacio) {
                super.updateItem(l, vacio);
                setGraphic(vacio || l == null ? null : b);
            }
        });
        tabla.setEditable(true);
        tabla.setItems(lineas);
        tabla.setPlaceholder(new Label("Use \"+ Agregar producto\" para añadir artículos a la compra"));
        total();
    }

    private void total() {
        lblTotal.setText(Fmt.moneda(lineas.stream().mapToDouble(LineaDetalle::getSubtotal).sum()));
    }

    @FXML
    private void nuevoProveedor() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Nuevo proveedor");
        d.setHeaderText("Registrar proveedor");
        TextField nombre = new TextField();
        TextField contacto = new TextField();
        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.addRow(0, new Label("Nombre *"), nombre);
        g.addRow(1, new Label("Contacto"), contacto);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> r = d.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;
        try {
            Contexto.get().proveedores.registrar(nombre.getText(), contacto.getText());
            cmbProveedor.getItems().setAll(Contexto.get().proveedores.nombres());
            cmbProveedor.setValue(nombre.getText().trim());
        } catch (NegocioException e) {
            Alertas.error(e.getMessage());
        }
    }

    @FXML
    private void agregarProducto() {
        Alertas.ocultarError(lblError);
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Agregar producto");
        d.setHeaderText("Producto, cantidad y costo unitario");
        ComboBox<Producto> cmb = new ComboBox<>(FXCollections.observableArrayList(Contexto.get().inventario.getProductos()));
        cmb.setPrefWidth(320);
        cmb.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p) { return p == null ? "" : p.getCodigo() + " – " + p.getNombre(); }
            @Override public Producto fromString(String s) { return null; }
        });
        TextField cant = new TextField("1");
        TextField costo = new TextField();
        cmb.valueProperty().addListener((o, a, p) -> { if (p != null) costo.setText(String.valueOf((long) p.getPrecio())); });
        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.addRow(0, new Label("Producto *"), cmb);
        g.addRow(1, new Label("Cantidad *"), cant);
        g.addRow(2, new Label("Costo unitario *"), costo);
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> r = d.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;
        try {
            Producto p = cmb.getValue();
            if (p == null) throw new NegocioException("Seleccione un producto");
            int c = Fmt.parseEntero(cant.getText(), "Cantidad");
            double cu = Fmt.parseDouble(costo.getText());
            if (c <= 0) throw new NegocioException("La cantidad debe ser mayor a 0");
            if (cu < 0) throw new NegocioException("El costo no puede ser negativo");
            for (LineaDetalle l : lineas) {
                if (l.getCodigo().equals(p.getCodigo())) throw new NegocioException("El producto ya está en la lista; edite su cantidad");
            }
            lineas.add(new LineaDetalle(p.getCodigo(), p.getNombre(), c, cu));
            total();
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        lineas.clear();
        cmbProveedor.setValue(null);
        dpFecha.setValue(LocalDate.now());
        Alertas.ocultarError(lblError);
        total();
    }

    @FXML
    private void registrar() {
        Alertas.ocultarError(lblError);
        try {
            Compra c = Contexto.get().compras.registrar(cmbProveedor.getValue(), dpFecha.getValue(), new ArrayList<>(lineas));
            lineas.clear();
            total();
            Alertas.comprobante(tabla.getScene().getWindow(), "Comprobante de compra N° " + c.getId(),
                    Comprobante.compra(c), "compra-" + c.getId() + ".pdf");
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
        }
    }
}
