package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.TipoReporte;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.Fmt;
import com.repuestosanelisa.util.NegocioException;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;


public class ReportesController {
    @FXML private ToggleGroup grupoTipo;
    @FXML private RadioButton rbInventario, rbVentas, rbCompras;
    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private Label lblError;
    @FXML private TextArea areaVista;

    private String ultimoTexto;
    private TipoReporte ultimoTipo;

    @FXML
    private void initialize() {

        dpDesde.setConverter(Fmt.convertidorFecha());
        dpHasta.setConverter(Fmt.convertidorFecha());
        dpDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dpHasta.setValue(LocalDate.now());
        cmbCategoria.getItems().add(Inventario.TODAS);
        cmbCategoria.getItems().addAll(Contexto.get().inventario.categorias());
        cmbCategoria.setValue(Inventario.TODAS);
        grupoTipo.selectedToggleProperty().addListener((o, a, b) -> {
            boolean inv = rbInventario.isSelected();
            dpDesde.setDisable(inv);
            dpHasta.setDisable(inv);
            cmbCategoria.setDisable(!inv);
        });
        cmbCategoria.setDisable(true);
    }

    private TipoReporte tipo() {
        return rbInventario.isSelected() ? TipoReporte.INVENTARIO
                : rbCompras.isSelected() ? TipoReporte.COMPRAS : TipoReporte.VENTAS;
    }

    @FXML
    private void generar() {
        Alertas.ocultarError(lblError);
        try {
            TipoReporte t = tipo();
            if (t != TipoReporte.INVENTARIO) {
                if (dpDesde.getValue() == null || dpHasta.getValue() == null)
                    throw new NegocioException("Seleccione el rango de fechas");
                if (dpDesde.getValue().isAfter(dpHasta.getValue()))
                    throw new NegocioException("La fecha inicial no puede ser posterior a la final");
            }
            ultimoTexto = Contexto.get().reportes.generar(t, dpDesde.getValue(), dpHasta.getValue(), cmbCategoria.getValue());
            ultimoTipo = t;
            areaVista.setText(ultimoTexto);
            // La vista previa también se guarda como PDF en un solo paso.
            Alertas.guardarPdf(areaVista.getScene().getWindow(), "reporte-" + t.name().toLowerCase() + ".pdf", ultimoTexto);
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
        }
    }

    @FXML
    private void guardarPdf() {
        if (ultimoTexto == null) { Alertas.mostrarError(lblError, "Primero genere el reporte"); return; }
        Alertas.guardarPdf(areaVista.getScene().getWindow(), "reporte-" + ultimoTipo.name().toLowerCase() + ".pdf", ultimoTexto);
    }

    @FXML
    private void imprimir() {
        if (ultimoTexto == null) { Alertas.mostrarError(lblError, "Primero genere el reporte"); return; }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) { Alertas.error("No hay impresora disponible. Use \"Guardar PDF\"."); return; }
        if (job.showPrintDialog(areaVista.getScene().getWindow())) {
            Text t = new Text(ultimoTexto);
            t.setFont(Font.font("Monospaced", 9));
            if (job.printPage(t)) job.endJob();
        }
    }

    @FXML private void cerrar() { Navegacion.ir(Modulo.INICIO); }
}
