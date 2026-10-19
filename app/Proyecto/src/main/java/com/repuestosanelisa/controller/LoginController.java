
package com.repuestosanelisa.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.IOException;

public class LoginController {

    @FXML
    private void iniciarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/repuestosanelisa/view/inventario.fxml"
                    )
            );

            Scene scene = new Scene(loader.load(), 800, 500);

            Stage ventana = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            ventana.setTitle("Repuestos Anelisa - Inventario");
            ventana.setScene(scene);
            ventana.setResizable(true);
            ventana.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
