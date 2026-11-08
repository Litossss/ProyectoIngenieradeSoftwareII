package com.repuestosanelisa.controller;

import com.repuestosanelisa.App;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.NegocioException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Autenticador (RF06). */
public class LoginController {
    @FXML private TextField usuarioField;
    @FXML private PasswordField contrasenaField;
    @FXML private Label lblError;

    @FXML
    private void iniciarSesion() {
        Alertas.ocultarError(lblError);
        try {
            Contexto.get().usuarios.login(usuarioField.getText(), contrasenaField.getText());
            App.mostrarPrincipal((Stage) usuarioField.getScene().getWindow());
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
            contrasenaField.clear();
        } catch (Exception e) {
            Alertas.mostrarError(lblError, "No se pudo abrir la aplicación: " + e.getMessage());
        }
    }
}
