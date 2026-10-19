
package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.Producto;

import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.Optional;

public class InventarioController {

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Integer> colCantidad;

    @FXML
    private TableColumn<Producto, Double> colPrecio;

    private Inventario inventario;

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio"));

        try {
            inventario = new Inventario();
            tablaProductos.setItems(inventario.getProductos());
        } catch (IOException e) {
            mostrarAlerta(
                    "No se pudo cargar el inventario: "
                            + e.getMessage());
        }
    }

    @FXML
    private void agregarProducto() {
        if (inventario == null) {
            mostrarAlerta("El inventario no está disponible.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Agregar producto");
        dialog.setHeaderText("Registro de repuesto");
        dialog.setContentText(
                "Código;Nombre;Cantidad;Precio\n"
                        + "Ejemplo: REP004;Cadena;10;35000");

        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(texto -> {
            try {
                String[] datos = texto.split(";", -1);

                if (datos.length != 4
                        || datos[0].trim().isEmpty()
                        || datos[1].trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Ingrese los cuatro datos correctamente.");
                }

                String codigo = datos[0].trim();
                String nombre = datos[1].trim();
                int cantidad = Integer.parseInt(datos[2].trim());
                double precio = Double.parseDouble(datos[3].trim());

                if (cantidad < 0 || precio < 0) {
                    throw new IllegalArgumentException(
                            "Cantidad y precio no pueden ser negativos.");
                }

                inventario.agregarProducto(
                        new Producto(codigo, nombre, cantidad, precio));

            } catch (NumberFormatException e) {
                mostrarAlerta(
                        "Cantidad debe ser entera y precio numérico.");
            } catch (IllegalArgumentException | IOException e) {
                mostrarAlerta(e.getMessage());
            }
        });
    }

    @FXML
    private void editarProducto() {
        if (inventario == null) {
            return;
        }

        Producto seleccionado = tablaProductos
                .getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Seleccione un producto para editar.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(
                seleccionado.getNombre() + ";"
                        + seleccionado.getCantidad() + ";"
                        + seleccionado.getPrecio());

        dialog.setTitle("Editar producto");
        dialog.setHeaderText(
                "Editar " + seleccionado.getCodigo());
        dialog.setContentText("Nombre;Cantidad;Precio");

        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(texto -> {
            try {
                String[] datos = texto.split(";", -1);

                if (datos.length != 3 || datos[0].trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Ingrese nombre, cantidad y precio.");
                }

                int cantidad = Integer.parseInt(datos[1].trim());
                double precio = Double.parseDouble(datos[2].trim());

                inventario.actualizarProducto(
                        seleccionado,
                        datos[0].trim(),
                        cantidad,
                        precio);

                tablaProductos.refresh();

            } catch (NumberFormatException e) {
                mostrarAlerta(
                        "Cantidad debe ser entera y precio numérico.");
            } catch (IllegalArgumentException | IOException e) {
                mostrarAlerta(e.getMessage());
            }
        });
    }

    @FXML
    private void eliminarProducto() {
        if (inventario == null) {
            return;
        }

        Producto seleccionado = tablaProductos
                .getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Seleccione un producto para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText("¿Desea eliminar este repuesto?");
        confirmacion.setContentText(
                seleccionado.getCodigo() + " - "
                        + seleccionado.getNombre());

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isPresent()
                && respuesta.get() == ButtonType.OK) {
            try {
                inventario.eliminarProducto(seleccionado);
            } catch (IOException e) {
                mostrarAlerta(e.getMessage());
            }
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Inventario");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
