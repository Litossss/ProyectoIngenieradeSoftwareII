package com.repuestosanelisa;

import com.repuestosanelisa.service.Contexto;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Contexto.get(); // carga los XML (o crea los datos de demostración)
        stage.setTitle("Repuestos Anelisa – Control de Inventario");
        mostrarLogin(stage);
        stage.show();
    }

    public static void mostrarLogin(Stage stage) throws IOException {
        Scene escena = new Scene(cargar("login.fxml"), 560, 640);
        stage.setScene(escena);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
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
