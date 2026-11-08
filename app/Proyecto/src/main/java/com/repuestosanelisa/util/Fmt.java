package com.repuestosanelisa.util;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class Fmt {
    public static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Fmt() {}

    public static String moneda(double v) {
        return v == Math.rint(v) ? String.format(Locale.US, "$ %,.0f", v) : String.format(Locale.US, "$ %,.2f", v);
    }

    public static LocalDate parseFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (Exception e) {
            throw new NegocioException("Fecha inválida: use el formato dd/mm/aaaa");
        }
    }

    public static double parseDouble(String texto) {
        try {
            double v = Double.parseDouble(texto.trim().replace(',', '.'));
            if (v < 0 || Double.isNaN(v) || Double.isInfinite(v)) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            throw new NegocioException("Ingrese un valor numérico válido (mayor o igual a 0)");
        }
    }

    public static int parseEntero(String texto, String campo) {
        try {
            int v = Integer.parseInt(texto.trim());
            if (v < 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            throw new NegocioException(campo + " debe ser un número entero mayor o igual a 0");
        }
    }

    public static String cortar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "~";
    }

    /** Convertidor dd/mm/aaaa para los DatePicker. */
    public static StringConverter<LocalDate> convertidorFecha() {
        return new StringConverter<>() {
            @Override public String toString(LocalDate d) { return d == null ? "" : FECHA.format(d); }

            @Override public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) return null;
                try { return LocalDate.parse(s.trim(), FECHA); } catch (Exception e) { return null; }
            }
        };
    }
}
