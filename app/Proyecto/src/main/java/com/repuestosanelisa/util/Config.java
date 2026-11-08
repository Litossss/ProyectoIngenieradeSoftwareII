package com.repuestosanelisa.util;

import java.nio.file.Path;

/**
 * Configuración de la aplicación.
 *
 * La conexión a MySQL puede cambiarse sin tocar el código con variables de entorno
 * (ANELISA_DB_URL, ANELISA_DB_USER, ANELISA_DB_PASSWORD) o con propiedades de la JVM
 * (-Danelisa.db.url=..., -Danelisa.db.user=..., -Danelisa.db.password=...).
 */
public final class Config {
    /** Carpeta local de la aplicación: solo guarda las fotos de los productos (fotos/). Ya está en tu .gitignore. */
    public static final Path DATA_DIR = Path.of("app-data");
    /** Con esta cantidad o menos se resalta la alerta de poco stock. */
    public static final int UMBRAL_POCO_STOCK = 5;

    /** Datos que crea el script SQL de configuracion-mysql.txt. */
    public static final String DB_URL = valor("ANELISA_DB_URL", "anelisa.db.url",
            "jdbc:mysql://localhost:3306/repuestos_anelisa"
                    + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Bogota"
                    + "&allowPublicKeyRetrieval=true&useSSL=false");
    public static final String DB_USER = valor("ANELISA_DB_USER", "anelisa.db.user", "anelisa_app");
    public static final String DB_PASSWORD = valor("ANELISA_DB_PASSWORD", "anelisa.db.password", "Anelisa#2026");

    private Config() {}

    private static String valor(String variable, String propiedad, String porDefecto) {
        String v = System.getProperty(propiedad);
        if (v == null || v.isBlank()) v = System.getenv(variable);
        return v == null || v.isBlank() ? porDefecto : v;
    }
}
