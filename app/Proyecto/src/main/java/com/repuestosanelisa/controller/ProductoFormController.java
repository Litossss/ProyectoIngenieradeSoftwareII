package com.repuestosanelisa.controller;

import com.repuestosanelisa.model.Inventario;
import com.repuestosanelisa.model.Producto;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.service.Contexto;
import com.repuestosanelisa.util.Alertas;
import com.repuestosanelisa.util.Fmt;
import com.repuestosanelisa.util.FotoUtil;
import com.repuestosanelisa.util.NegocioException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

/** Pantalla 4 – Formulario para registrar o editar un producto (RF01). */
public class ProductoFormController {
    @FXML private TextField txtCodigo, txtNombre, txtCantidad, txtPrecio;
    @FXML private ComboBox<String> cmbCategoria, cmbProveedor;
    @FXML private TextArea txtDescripcion;
    @FXML private ImageView imgFoto;
    @FXML private Label lblSinFoto, lblError;

    private final Inventario inventario = Contexto.get().inventario;
    private Producto editando;
    private String foto = "";
    private File fotoNueva;

    @FXML
    private void initialize() {
        cmbCategoria.getItems().addAll(inventario.categorias());
        cmbProveedor.getItems().addAll(Contexto.get().proveedores.nombres());
    }

    /** null = nuevo producto; en otro caso se precargan los datos para editar. */
    public void iniciar(Producto p) {
        editando = p;
        if (p == null) return;
        txtCodigo.setText(p.getCodigo());
        txtCodigo.setDisable(true); // el código identifica al producto: no se cambia
        txtNombre.setText(p.getNombre());
        cmbCategoria.setValue(p.getCategoria());
        txtCantidad.setText(String.valueOf(p.getCantidad()));
        txtPrecio.setText(String.valueOf((long) p.getPrecio() == p.getPrecio() ? (Object) (long) p.getPrecio() : (Object) p.getPrecio()));
        cmbProveedor.setValue(p.getProveedor() == null || p.getProveedor().isBlank() ? null : p.getProveedor());
        txtDescripcion.setText(p.getDescripcion());
        foto = p.getFoto() == null ? "" : p.getFoto();
        var img = FotoUtil.cargar(foto, 220, 190);
        if (img != null) { imgFoto.setImage(img); lblSinFoto.setVisible(false); }
    }

    @FXML
    private void elegirFoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar fotografía");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(txtCodigo.getScene().getWindow());
        if (f == null) return;
        var img = FotoUtil.cargarArchivo(f, 220, 190);
        if (img == null || img.isError()) { Alertas.mostrarError(lblError, "No se pudo leer la imagen seleccionada"); return; }
        fotoNueva = f;
        imgFoto.setImage(img);
        lblSinFoto.setVisible(false);
    }

    @FXML private void cancelar() { Navegacion.ir(Modulo.INVENTARIO); }

    @FXML
    private void guardar() {
        Alertas.ocultarError(lblError);
        try {
            String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
            String categoria = cmbCategoria.getEditor().getText() == null ? "" : cmbCategoria.getEditor().getText().trim();
            if (codigo.isEmpty()) throw new NegocioException("El código es obligatorio");
            if (txtNombre.getText().isBlank()) throw new NegocioException("El nombre es obligatorio");
            if (categoria.isEmpty()) throw new NegocioException("La categoría es obligatoria");
            int cantidad = Fmt.parseEntero(txtCantidad.getText(), "Cantidad");
            double precio = Fmt.parseDouble(txtPrecio.getText());
            if (cantidad < 0) throw new NegocioException("La cantidad no puede ser negativa");
            if (precio < 0) throw new NegocioException("El precio no puede ser negativo");

            String fotoFinal = fotoNueva != null ? FotoUtil.guardar(fotoNueva, codigo) : foto;
            String prov = cmbProveedor.getValue() == null ? "" : cmbProveedor.getValue();
            Producto datos = new Producto(codigo, txtNombre.getText().trim(), categoria, cantidad, precio,
                    prov, txtDescripcion.getText() == null ? "" : txtDescripcion.getText().trim(), fotoFinal);

            if (editando == null) inventario.agregarProducto(datos);
            else inventario.actualizarProducto(datos);
            Alertas.info(editando == null ? "Producto registrado correctamente." : "Producto actualizado correctamente.");
            Navegacion.ir(Modulo.INVENTARIO);
        } catch (NegocioException e) {
            Alertas.mostrarError(lblError, e.getMessage());
        }
    }
}
