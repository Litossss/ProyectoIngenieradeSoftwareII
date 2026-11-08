package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Proveedor;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.NegocioException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProveedoresController {
    @FXML private ToggleGroup grupoEstado;
    @FXML private TableView<Proveedor> tabla;
    @FXML private TableColumn<Proveedor, String> colNombre, colContacto, colEstado;
    @FXML private TableColumn<Proveedor, Proveedor> colAcciones;
    @FXML private Label lblModo, lblError;
    @FXML private TextField txtNombre, txtContacto;
    @FXML private RadioButton rbActivo, rbInactivo;

    private String nombreEditando;   // null = creando uno nuevo

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colContacto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContacto()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colAcciones.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("Editar");
            { b.getStyleClass().add("btn-mini"); b.setOnAction(e -> editar(getItem())); }
            @Override protected void updateItem(Proveedor p, boolean vacio) {
                super.updateItem(p, vacio);
                setGraphic(vacio || p == null ? null : b);
            }
        });
        tabla.setPlaceholder(new Label("No hay proveedores registrados"));
        refrescar();
        nuevoProveedor();
    }

    private void refrescar() {
        tabla.setItems(FXCollections.observableArrayList(Contexto.get().proveedores.listar()));
    }

    @FXML
    private void nuevoProveedor() {
        nombreEditando = null;
        lblModo.setText("Crear proveedor");
        txtNombre.clear();
        txtContacto.clear();
        rbActivo.setSelected(true);
        rbActivo.setDisable(true);     // un proveedor nuevo siempre nace activo
        rbInactivo.setDisable(true);
        Alertas.ocultarError(lblError);
    }

    private void editar(Proveedor p) {
        nombreEditando = p.getNombre();
        lblModo.setText("Editar proveedor");
        txtNombre.setText(p.getNombre());
        txtContacto.setText(p.getContacto());
        rbActivo.setDisable(false);
        rbInactivo.setDisable(false);
        (p.isActivo() ? rbActivo : rbInactivo).setSelected(true);
        Alertas.ocultarError(lblError);
    }

    @FXML private void cancelar() { nuevoProveedor(); }

    @FXML
    private void guardar() {
        Alertas.ocultarError(lblError);
        try {
            var servicio = Contexto.get().proveedores;
            if (nombreEditando == null) {
                servicio.registrar(txtNombre.getText(), txtContacto.getText());
                Alertas.info("Proveedor creado correctamente.");
            } else {
                servicio.editar(nombreEditando, txtNombre.getText(), txtContacto.getText(), rbActivo.isSelected());
                Contexto.get().inventario.recargar();   // los productos muestran el nombre actualizado
                Alertas.info("Proveedor actualizado correctamente.");
            }
            refrescar();
            nuevoProveedor();
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
        }
    }
}
