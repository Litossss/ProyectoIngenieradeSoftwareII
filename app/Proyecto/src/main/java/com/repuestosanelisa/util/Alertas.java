package com.repuestosanelisa.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public final class Alertas {
    private Alertas() {}

    public static void info(String mensaje) { alerta(Alert.AlertType.INFORMATION, mensaje); }

    public static void error(String mensaje) { alerta(Alert.AlertType.WARNING, mensaje); }

    private static void alerta(Alert.AlertType tipo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle("Repuestos Anelisa");
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    public static boolean confirmar(String titulo, String encabezado, String contenido) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(titulo);
        a.setHeaderText(encabezado);
        a.setContentText(contenido);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    public static void comprobante(Window owner, String titulo, String texto, String archivoSugerido) {
        TextArea area = new TextArea(texto);
        area.setEditable(false);
        area.setFont(Font.font("Monospaced", 12));
        area.setPrefSize(620, 360);

        ButtonType guardar = new ButtonType("Guardar PDF", ButtonBar.ButtonData.LEFT);
        Dialog<ButtonType> d = new Dialog<>();
        d.initOwner(owner);
        d.setTitle(titulo);
        d.getDialogPane().setContent(area);
        d.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CLOSE);
        Optional<ButtonType> r = d.showAndWait();
        if (r.isPresent() && r.get() == guardar) guardarPdf(owner, archivoSugerido, texto);
    }

    public static void guardarPdf(Window owner, String archivoSugerido, String texto) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF");
        fc.setInitialFileName(archivoSugerido);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File f = fc.showSaveDialog(owner);
        if (f == null) return;
        if (!f.getName().toLowerCase().endsWith(".pdf")) f = new File(f.getPath() + ".pdf");
        try {
            PdfUtil.guardar(f.toPath(), texto);
            info("PDF guardado en:\n" + f.getAbsolutePath());
        } catch (IOException e) {
            error("No se pudo guardar el PDF: " + e.getMessage());
        }
    }

    public static void mostrarError(Label caja, String mensaje) {
        caja.setText(mensaje);
        caja.setVisible(true);
        caja.setManaged(true);
    }

    public static void ocultarError(Label caja) {
        caja.setText("");
        caja.setVisible(false);
        caja.setManaged(false);
    }
}
