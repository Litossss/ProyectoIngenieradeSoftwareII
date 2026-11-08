package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Rol;
import com.repuestosanelisa.model.Usuario;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.NegocioException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class UsuariosController {
    @FXML private ToggleGroup grupoEstado;
    @FXML private TableView<Usuario> tabla;
    @FXML private TableColumn<Usuario, String> colNombre, colUsuario, colRol, colEstado;
    @FXML private TableColumn<Usuario, Usuario> colAcciones;
    @FXML private Label lblPermisos, lblModo, lblContrasena, lblError;
    @FXML private TextField txtNombre, txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private RadioButton rbActivo, rbInactivo;

    private Usuario editando;

    @FXML
    private void initialize() {
        cmbRol.getItems().setAll(Rol.values());
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuario()));
        colRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRol().toString()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colAcciones.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colAcciones.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("Editar");
            { b.getStyleClass().add("btn-mini"); b.setOnAction(e -> editar(getItem())); }
            @Override protected void updateItem(Usuario u, boolean vacio) {
                super.updateItem(u, vacio);
                setGraphic(vacio || u == null ? null : b);
            }
        });
        StringBuilder sb = new StringBuilder();
        for (Rol r : Rol.values()) sb.append(r).append(": ").append(r.getPermisos()).append("\n");
        lblPermisos.setText(sb.toString().trim());
        refrescar();
        nuevoUsuario();
    }

    private void refrescar() {
        tabla.setItems(FXCollections.observableArrayList(Contexto.get().usuarios.listar()));
    }

    @FXML
    private void nuevoUsuario() {
        editando = null;
        lblModo.setText("Crear usuario");
        lblContrasena.setText("Contraseña *");
        txtNombre.clear();
        txtUsuario.clear();
        txtUsuario.setDisable(false);
        txtContrasena.clear();
        txtContrasena.setPromptText("Mínimo 4 caracteres");
        cmbRol.setValue(null);
        rbActivo.setSelected(true);
        Alertas.ocultarError(lblError);
    }

    private void editar(Usuario u) {
        editando = u;
        lblModo.setText("Editar usuario");
        lblContrasena.setText("Contraseña (vacío = no cambiar)");
        txtNombre.setText(u.getNombre());
        txtUsuario.setText(u.getUsuario());
        txtUsuario.setDisable(true);
        txtContrasena.clear();
        txtContrasena.setPromptText("Dejar vacío para conservar");
        cmbRol.setValue(u.getRol());
        (u.isActivo() ? rbActivo : rbInactivo).setSelected(true);
        Alertas.ocultarError(lblError);
    }

    @FXML private void cancelar() { nuevoUsuario(); }

    @FXML
    private void guardar() {
        Alertas.ocultarError(lblError);
        try {
            boolean activo = rbActivo.isSelected();
            if (editando == null) {
                Contexto.get().usuarios.crear(txtNombre.getText(), txtUsuario.getText(),
                        txtContrasena.getText(), cmbRol.getValue(), activo);
                Alertas.info("Usuario creado correctamente.");
            } else {
                Contexto.get().usuarios.editar(editando.getUsuario(), txtNombre.getText(),
                        txtContrasena.getText(), cmbRol.getValue(), activo);
                Alertas.info("Usuario actualizado correctamente.");
            }
            refrescar();
            nuevoUsuario();
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
        }
    }
}
