package com.repuestosanelisa.util;

import java.nio.file.Path;


public final class Config {
    
    public static final Path DATA_DIR = Path.of("app-data");
    public static final int UMBRAL_POCO_STOCK = 5;

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