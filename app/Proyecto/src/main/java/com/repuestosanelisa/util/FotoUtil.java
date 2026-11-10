package com.repuestosanelisa.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class FotoUtil {
    private FotoUtil() {}


    public static Path carpeta() { return Config.DATA_DIR.resolve("fotos"); }

    public static String guardar(File origen, String codigo) {
        String nombre = origen.getName();
        int i = nombre.lastIndexOf('.');
        String ext = i < 0 ? "" : nombre.substring(i + 1).toLowerCase();
        if (!List.of("png", "jpg", "jpeg", "gif").contains(ext)) {
            throw new NegocioException("La fotografía debe ser PNG, JPG o GIF");
        }
        String destino = codigo.replaceAll("[^A-Za-z0-9_-]", "_") + "." + ext;
        try {
            Files.createDirectories(carpeta());
            Files.copy(origen.toPath(), carpeta().resolve(destino), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new NegocioException("No se pudo copiar la fotografía: " + e.getMessage());
        }
        return destino;
    }

    public static Image cargar(String nombre, double ancho, double alto) {
        if (nombre == null || nombre.isBlank()) return null;
        return cargarArchivo(carpeta().resolve(nombre).toFile(), ancho, alto);
    }

    public static Image cargarArchivo(File archivo, double ancho, double alto) {
        if (archivo == null || !archivo.exists()) return null;
        Image img = new Image(archivo.toURI().toString(), ancho, alto, true, true);
        return img.isError() ? null : img;
    }
}
