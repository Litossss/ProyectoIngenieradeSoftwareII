package com.repuestosanelisa.util;

import java.nio.file.Path;

public final class Config {
    public static final Path DATA_DIR = Path.of("app-data");
    public static final int UMBRAL_POCO_STOCK = 5;
    public static final boolean CARGAR_DATOS_DEMO = true;

    private Config() {}
}
