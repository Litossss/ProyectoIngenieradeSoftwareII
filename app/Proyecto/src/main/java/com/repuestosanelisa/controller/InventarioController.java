package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.Config;
import com.repuestosanelisa.util.FotoUtil;
import com.repuestosanelisa.util.Fmt;
import com.repuestosanelisa.util.NegocioException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.List;

/** Pantalla 3 – Inventario y consulta rápida de existencias (RF01 / RF05). */
public class InventarioController {
    private static final int POR_PAGINA = 10;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TableView<Producto> tabla;
    @FXML private TableColumn<Producto, String> colFoto, colCodigo, colNombre, colCategoria, colCantidad, colPrecio, colProveedor, colAcciones;
    @FXML private Label lblResumen, lblPagina;
    @FXML private Button btnAnterior, btnSiguiente;

    private final Inventario inventario = Contexto.get().inventario;
    private List<Producto> resultados = List.of();
    private int pagina = 0;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria()));
        colCantidad.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        colPrecio.setCellValueFactory(c -> new SimpleStringProperty(Fmt.moneda(c.getValue().getPrecio())));
        colProveedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProveedor()));
        colFoto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFoto()));
        colAcciones.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colFoto.setCellFactory(c -> new CeldaFoto());
        colCantidad.setCellFactory(c -> new CeldaCantidad());
        colAcciones.setCellFactory(c -> new CeldaAcciones());
        tabla.setPlaceholder(new Label("No se encontraron productos"));
        tabla.setFixedCellSize(46);

        cmbCategoria.getItems().add(Inventario.TODAS);
        cmbCategoria.getItems().addAll(inventario.categorias());
        cmbCategoria.setValue(Inventario.TODAS);
        cmbCategoria.setOnAction(e -> buscar());
        buscar();
    }

    @FXML
    private void buscar() {
        resultados = inventario.buscar(txtBuscar.getText(), cmbCategoria.getValue());
        pagina = 0;
        mostrarPagina();
    }

    @FXML private void paginaAnterior()  { if (pagina > 0) { pagina--; mostrarPagina(); } }
    @FXML private void paginaSiguiente() { if ((pagina + 1) * POR_PAGINA < resultados.size()) { pagina++; mostrarPagina(); } }
    @FXML private void nuevoProducto()   { Navegacion.abrirFormularioProducto(null); }

    private void mostrarPagina() {
        int total = resultados.size();
        int paginas = Math.max(1, (int) Math.ceil(total / (double) POR_PAGINA));
        int desde = pagina * POR_PAGINA;
        int hasta = Math.min(total, desde + POR_PAGINA);
        tabla.setItems(FXCollections.observableArrayList(resultados.subList(desde, hasta)));
        lblPagina.setText((pagina + 1) + " / " + paginas);
        lblResumen.setText(total == 0 ? "0 resultados"
                : "Mostrando " + (desde + 1) + "–" + hasta + " de " + total + " producto(s)");
        btnAnterior.setDisable(pagina == 0);
        btnSiguiente.setDisable(pagina + 1 >= paginas);
    }

    private void eliminar(Producto p) {
        if (!Alertas.confirmar("Eliminar producto", "¿Eliminar \"" + p.getNombre() + "\"?",
                "Esta acción no se puede deshacer.")) return;
        try {
            inventario.eliminarProducto(p);
            buscar();
        } catch (NegocioException e) {
            Alertas.error(e.getMessage());
        }
    }

    /** Miniatura del producto (o guion si no tiene foto). */
    private static class CeldaFoto extends TableCell<Producto, String> {
        private final ImageView vista = new ImageView();
        CeldaFoto() { vista.setFitWidth(38); vista.setFitHeight(38); vista.setPreserveRatio(true); setAlignment(Pos.CENTER); }
        @Override protected void updateItem(String foto, boolean vacio) {
            super.updateItem(foto, vacio);
            if (vacio) { setGraphic(null); setText(null); return; }
            var img = (foto == null || foto.isBlank()) ? null : FotoUtil.cargar(foto, 38, 38);
            if (img == null) { setGraphic(null); setText("—"); }
            else { vista.setImage(img); setText(null); setGraphic(vista); }
        }
    }

    /** Resalta en rojo el stock bajo (alerta). */
    private static class CeldaCantidad extends TableCell<Producto, String> {
        @Override protected void updateItem(String valor, boolean vacio) {
            super.updateItem(valor, vacio);
            getStyleClass().remove("stock-alerta");
            if (vacio || valor == null) { setText(null); return; }
            setText(valor);
            if (Integer.parseInt(valor) <= Config.UMBRAL_POCO_STOCK) getStyleClass().add("stock-alerta");
        }
    }

    private class CeldaAcciones extends TableCell<Producto, String> {
        private final Button editar = new Button("Editar");
        private final Button borrar = new Button("Elim.");
        private final HBox caja = new HBox(6, editar, borrar);
        CeldaAcciones() {
            editar.getStyleClass().add("btn-mini");
            borrar.getStyleClass().add("btn-mini");
            caja.setAlignment(Pos.CENTER_LEFT);
            editar.setOnAction(e -> Navegacion.abrirFormularioProducto(getTableView().getItems().get(getIndex())));
            borrar.setOnAction(e -> eliminar(getTableView().getItems().get(getIndex())));
            borrar.setVisible(Permisos.esAdmin());
            borrar.setManaged(Permisos.esAdmin());
        }
        @Override protected void updateItem(String codigo, boolean vacio) {
            super.updateItem(codigo, vacio);
            setGraphic(vacio ? null : caja);
        }
    }
}
