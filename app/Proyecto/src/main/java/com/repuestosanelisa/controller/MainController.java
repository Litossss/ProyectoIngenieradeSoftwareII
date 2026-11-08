package com.repuestosanelisa.controller;

import com.repuestosanelisa.App;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.util.Alertas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Marco principal: menú lateral según el rol y zona donde se cargan las vistas. */
public class MainController {
    @FXML private ToggleButton btnInicio, btnInventario, btnVentas, btnCompras, btnProveedores, btnReportes, btnUsuarios;
    @FXML private Label lblTitulo, lblUsuario;
    @FXML private StackPane contenido;

    @FXML
    private void initialize() {
        Navegacion.registrar(this);
        var u = Sesion.actual();
        lblUsuario.setText(u.getNombre() + " (" + u.getRol() + ")");
        ocultarSiNoPermitido(btnInicio, Modulo.INICIO);
        ocultarSiNoPermitido(btnInventario, Modulo.INVENTARIO);
        ocultarSiNoPermitido(btnVentas, Modulo.VENTAS);
        ocultarSiNoPermitido(btnCompras, Modulo.COMPRAS);
        ocultarSiNoPermitido(btnProveedores, Modulo.PROVEEDORES);
        ocultarSiNoPermitido(btnReportes, Modulo.REPORTES);
        ocultarSiNoPermitido(btnUsuarios, Modulo.USUARIOS);
        ir(Modulo.INICIO);
    }

    private void ocultarSiNoPermitido(ToggleButton b, Modulo m) {
        boolean ok = Permisos.puede(Sesion.actual().getRol(), m);
        b.setVisible(ok);
        b.setManaged(ok);
    }

    @FXML private void irInicio()     { ir(Modulo.INICIO); }
    @FXML private void irInventario() { ir(Modulo.INVENTARIO); }
    @FXML private void irVentas()     { ir(Modulo.VENTAS); }
    @FXML private void irCompras()    { ir(Modulo.COMPRAS); }
    @FXML private void irProveedores() { ir(Modulo.PROVEEDORES); }
    @FXML private void irReportes()   { ir(Modulo.REPORTES); }
    @FXML private void irUsuarios()   { ir(Modulo.USUARIOS); }

    void ir(Modulo modulo) {
        if (!Permisos.puede(Sesion.actual().getRol(), modulo)) {
            Alertas.error("Su rol no tiene acceso al módulo " + modulo + ".");
            return;
        }
        ToggleButton boton = switch (modulo) {
            case INICIO -> btnInicio;
            case INVENTARIO -> btnInventario;
            case VENTAS -> btnVentas;
            case COMPRAS -> btnCompras;
            case PROVEEDORES -> btnProveedores;
            case REPORTES -> btnReportes;
            case USUARIOS -> btnUsuarios;
        };
        boton.setSelected(true);
        String vista = switch (modulo) {
            case INICIO -> "inicio";
            case INVENTARIO -> "inventario";
            case VENTAS -> "ventas";
            case COMPRAS -> "compras";
            case PROVEEDORES -> "proveedores";
            case REPORTES -> "reportes";
            case USUARIOS -> "usuarios";
        };
        cargar(vista, modulo.toString());
    }

    void abrirFormularioProducto(Producto p) {
        btnInventario.setSelected(true);
        Object ctrl = cargar("producto-form", p == null ? "Registrar producto" : "Editar producto");
        ((ProductoFormController) ctrl).iniciar(p);
    }

    private Object cargar(String vista, String titulo) {
        try {
            java.net.URL ubicacion = App.class.getResource("view/" + vista + ".fxml");
            if (ubicacion == null) {
                throw new IllegalStateException("No se encuentra el archivo " + vista + ".fxml en "
                        + "src/main/resources/com/repuestosanelisa/view/. Cópielo ahí y vuelva a compilar.");
            }
            FXMLLoader loader = new FXMLLoader(ubicacion);
            Node nodo = loader.load();
            contenido.getChildren().setAll(nodo);
            lblTitulo.setText(titulo);
            return loader.getController();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar la vista " + vista, e);
        }
    }

    @FXML
    private void salir() {
        if (!Alertas.confirmar("Cerrar sesión", "¿Desea salir?", "Se cerrará la sesión actual.")) return;
        Sesion.cerrar();
        try {
            App.mostrarLogin((Stage) contenido.getScene().getWindow());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
