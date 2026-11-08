package com.repuestosanelisa.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PdfUtil {
    private static final Charset WIN = Charset.forName("windows-1252");
    private static final int ANCHO = 595, ALTO = 842, MARGEN = 40, TAM = 9, INTERLINEA = 11;

    private PdfUtil() {}

    public static void guardar(Path ruta, String texto) throws IOException {
        int porPagina = (ALTO - 2 * MARGEN - 10) / INTERLINEA;
        List<String> lineas = new ArrayList<>(texto.lines().toList());
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i += porPagina) {
            paginas.add(lineas.subList(i, Math.min(lineas.size(), i + porPagina)));
        }
        if (paginas.isEmpty()) paginas.add(List.of(""));
        int n = paginas.size();

        List<byte[]> objetos = new ArrayList<>();
        objetos.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < n; i++) kids.append(4 + 2 * i).append(" 0 R ");
        objetos.add(ascii("<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>"));
        objetos.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Courier /Encoding /WinAnsiEncoding >>"));
        for (int i = 0; i < n; i++) {
            objetos.add(ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + ANCHO + " " + ALTO
                    + "] /Resources << /Font << /F1 3 0 R >> >> /Contents " + (5 + 2 * i) + " 0 R >>"));
            byte[] contenido = contenido(paginas.get(i), i + 1, n);
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            s.write(ascii("<< /Length " + contenido.length + " >>\nstream\n"));
            s.write(contenido);
            s.write(ascii("\nendstream"));
            objetos.add(s.toByteArray());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ascii("%PDF-1.4\n"));
        int[] offsets = new int[objetos.size()];
        for (int k = 0; k < objetos.size(); k++) {
            offsets[k] = out.size();
            out.write(ascii((k + 1) + " 0 obj\n"));
            out.write(objetos.get(k));
            out.write(ascii("\nendobj\n"));
        }
        int xref = out.size();
        out.write(ascii("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n"));
        for (int off : offsets) out.write(ascii(String.format("%010d 00000 n \n", off)));
        out.write(ascii("trailer\n<< /Size " + (objetos.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n"));

        if (ruta.getParent() != null) Files.createDirectories(ruta.getParent());
        Files.write(ruta, out.toByteArray());
    }

    private static byte[] contenido(List<String> lineas, int num, int total) throws IOException {
        ByteArrayOutputStream c = new ByteArrayOutputStream();
        c.write(ascii("BT\n/F1 " + TAM + " Tf\n" + INTERLINEA + " TL\n" + MARGEN + " " + (ALTO - MARGEN - TAM) + " Td\n"));
        for (String l : lineas) {
            c.write('(');
            escribirEscapado(c, l);
            c.write(ascii(") Tj\nT*\n"));
        }
        c.write(ascii("ET\nBT\n/F1 8 Tf\n" + MARGEN + " 25 Td\n("));
        escribirEscapado(c, "Página " + num + " de " + total);
        c.write(ascii(") Tj\nET\n"));
        return c.toByteArray();
    }

    private static void escribirEscapado(ByteArrayOutputStream out, String s) {
        for (byte b : s.replace("\t", "    ").getBytes(WIN)) {
            if (b == '\\' || b == '(' || b == ')') out.write('\\');
            out.write(b);
        }
    }

    private static byte[] ascii(String s) { return s.getBytes(StandardCharsets.ISO_8859_1); }
}
