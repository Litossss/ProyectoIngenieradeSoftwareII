package com.repuestosanelisa;

import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.Db;
import com.repuestosanelisa.util.NegocioException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Db.verificar();
            Contexto.get();
        } catch (NegocioException e) {
            Alertas.error(e.getMessage());
            Platform.exit();
            return;
        }
        stage.setTitle("Repuestos Anelisa – Control de Inventario");
        var icono = App.class.getResourceAsStream("images/logo.jpg");
        if (icono != null) stage.getIcons().add(new javafx.scene.image.Image(icono));
        mostrarLogin(stage);
        stage.show();
    }

    public static void mostrarLogin(Stage stage) throws IOException {

        stage.setMaximized(false);
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setResizable(true);
        stage.setScene(new Scene(cargar("login.fxml"), 560, 640));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        Platform.runLater(() -> { stage.setMaximized(false); stage.sizeToScene(); stage.centerOnScreen(); });
    }

    public static void mostrarPrincipal(Stage stage) throws IOException {
        Scene escena = new Scene(cargar("main.fxml"), 1180, 720);
        stage.setResizable(true);
        stage.setScene(escena);
        stage.setMinWidth(1000);
        stage.setMinHeight(640);
        stage.centerOnScreen();
    }

    private static javafx.scene.Parent cargar(String vista) throws IOException {
        return new FXMLLoader(App.class.getResource("view/" + vista)).load();
    }

    public static void main(String[] args) { launch(args); }
}
