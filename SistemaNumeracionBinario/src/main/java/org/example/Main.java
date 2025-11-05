package org.example;

import javax.swing.JOptionPane;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.example.operaacionesAritmeticas.OperacionAritmetica;
import org.example.reloj.ConversorBase;
import org.example.reloj.MostrarReloj;
import org.example.reloj.TiempoBinario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private static final MostrarReloj reloj = new MostrarReloj();
    private static final ConversorBase conversor = new ConversorBase();
    private static final OperacionAritmetica calculadora = new OperacionAritmetica();

    // área: fila 1 = reloj, desde fila 3 menú y opciones (ajusta según tamaño)
    private static final int RELOJ_ROW = 1;
    private static final int MENU_START_ROW = 3;
    private static final int SCREEN_WIDTH = 100;

    // Scheduler para actualizar el reloj cada segundo
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Helpers de cálculo (copiados de tu lógica)
    private static int binarioATotalSegundos(TiempoBinario t) {
        int h = Integer.parseInt(t.hora, 2);
        int m = Integer.parseInt(t.minuto, 2);
        int s = Integer.parseInt(t.segundo, 2);
        return h * 3600 + m * 60 + s;
    }

    private static TiempoBinario segundosATiempoBinario(int totalSegundos) {
        totalSegundos = ((totalSegundos % 86400) + 86400) % 86400; // normalizar en un día

        int h = totalSegundos / 3600;
        int m = (totalSegundos % 3600) / 60;
        int s = totalSegundos % 60;

        String hBin = conversor.decimalABase(h, 2);
        String mBin = conversor.decimalABase(m, 2);
        String sBin = conversor.decimalABase(s, 2);

        return new TiempoBinario(hBin, mBin, sBin);
    }

    // Método para dibujar el bloque del reloj (compacto de 1 línea)
    private static void drawClock(TextGraphics tg) {
        int[] h = reloj.getHoraActual();
        String base10 = String.format("%02d:%02d:%02d", h[0], h[1], h[2]);
        String base2  = String.format("%s:%s:%s",
                conversor.decimalABase(h[0], 2),
                conversor.decimalABase(h[1], 2),
                conversor.decimalABase(h[2], 2));
        String base8  = String.format("%s:%s:%s",
                conversor.decimalABase(h[0], 8),
                conversor.decimalABase(h[1], 8),
                conversor.decimalABase(h[2], 8));
        String base16 = String.format("%s:%s:%s",
                conversor.decimalABase(h[0], 16),
                conversor.decimalABase(h[1], 16),
                conversor.decimalABase(h[2], 16));

        // Limpiar únicamente la línea donde se mostrará el reloj
        tg.putString(1, RELOJ_ROW, " ".repeat(Math.max(1, SCREEN_WIDTH)));

        // Escribir el reloj compacto en una sola línea
        String compact = String.format("Hora: %s | Bin: %s | Oct: %s | Hex: %s",
                base10, base2, base8, base16);

        // Si la cadena es más larga que el ancho, la acortamos para que no desborde
        if (compact.length() > SCREEN_WIDTH) compact = compact.substring(0, SCREEN_WIDTH);

        tg.putString(1, RELOJ_ROW, compact);
    }

    // Dibuja el menú (estático)
    private static void drawMenu(TextGraphics tg) {
        // limpiar espacio del menú
        for (int r = 0; r < 12; r++) tg.putString(1, MENU_START_ROW + r, " ".repeat(Math.max(1, SCREEN_WIDTH)));

        tg.setForegroundColor(TextColor.ANSI.YELLOW);
        tg.putString(1, MENU_START_ROW, "¨========Eliga una de las opciones: ========");
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.putString(1, MENU_START_ROW + 1, "1. Tomar horas del reloj automaticamente");
        tg.putString(1, MENU_START_ROW + 2, "2. Ingresar las horas manual ");
        tg.putString(1, MENU_START_ROW + 3, "3. Salir");
        tg.putString(1, MENU_START_ROW + 4, "Eliga una opcion: ");
        tg.setForegroundColor(TextColor.ANSI.CYAN);
        tg.putString(1, MENU_START_ROW + 6, "Presione 1 / 2 / 3. Para respuestas escritas, escriba y pulse Enter.");
        tg.setForegroundColor(TextColor.ANSI.WHITE);
    }

    // Lee una línea de entrada desde Lanterna (muestra el prompt en (row,col))
    private static String readLine(Screen screen, TextGraphics tg, int promptRow, int promptCol) throws IOException {
        StringBuilder sb = new StringBuilder();
        screen.refresh();
        while (true) {
            KeyStroke key = screen.pollInput();
            if (key == null) {
                // sin entrada, dormir brevemente
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                continue;
            }
            switch (key.getKeyType()) {
                case Character:
                    sb.append(key.getCharacter());
                    tg.putString(promptCol, promptRow, sb.toString() + " ");
                    screen.refresh();
                    break;
                case Backspace:
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        tg.putString(promptCol, promptRow, sb.toString() + " ");
                        screen.refresh();
                    }
                    break;
                case Enter:
                    return sb.toString().trim();
                case Escape:
                    return ""; // cancelar
                default:
                    // ignorar otras teclas
            }
        }
    }

    // Mostrar mensajes múltiples (limpia area debajo del menú y escribe)
    private static void writeLines(Screen screen, TextGraphics tg, int startRow, List<String> lines) {
        // Limpiar varias líneas
        for (int r = 0; r < 12; r++) {
            tg.putString(1, startRow + r, " ".repeat(Math.max(1, SCREEN_WIDTH)));
        }
        for (int i = 0; i < lines.size(); i++) {
            tg.putString(1, startRow + i, lines.get(i));
        }
        try { screen.refresh(); } catch (IOException ignored) {}
    }

    // Helper: leer un solo carácter (sin pedir Enter), con timeout en segundos.
    // Devuelve String con 1 carácter o null si timeout.
    private static String readSingleCharWithTimeout(Screen screen, TextGraphics tg, int promptRow, int promptCol, int timeoutSeconds) throws IOException {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        try { screen.refresh(); } catch (IOException ignored) {}
        while (System.currentTimeMillis() < deadline) {
            KeyStroke key = screen.pollInput(); // no bloqueante
            if (key == null) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                continue;
            }
            switch (key.getKeyType()) {
                case Character:
                    char c = key.getCharacter();
                    tg.putString(promptCol, promptRow, Character.toString(c) + " ");
                    try { screen.refresh(); } catch (IOException ignored) {}
                    return Character.toString(c);
                case Enter:
                    // si presionaron Enter sin carácter previo, ignorar y seguir esperando
                    break;
                case Backspace:
                    // ignorar
                    break;
                default:
                    // otras teclas: ignorar
            }
        }
        return null;
    }

    // Función helper: readLine con timeout en segundos.
    // - devuelve null si expira el timeout
    // - muestra los caracteres en promptCol,promptRow
    private static String readLineWithTimeout(Screen screen, TextGraphics tg, int promptRow, int promptCol, int timeoutSeconds) throws IOException {
        StringBuilder sb = new StringBuilder();
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        screen.refresh();

        while (System.currentTimeMillis() < deadline) {
            KeyStroke key = screen.pollInput(); // no bloqueante
            if (key == null) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                continue;
            }
            switch (key.getKeyType()) {
                case Character:
                    sb.append(key.getCharacter());
                    tg.putString(promptCol, promptRow, sb.toString() + " ");
                    screen.refresh();
                    break;
                case Backspace:
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length()-1);
                        tg.putString(promptCol, promptRow, sb.toString() + " ");
                        screen.refresh();
                    }
                    break;
                case Enter:
                    return sb.toString().trim();
                case Escape:
                    return "";
                default:
                    // ignorar otras teclas
            }
        }
        return null; // timeout
    }

    // ========== OPCIÓN 1 (horas automáticas) con fallback JOptionPane ==========
    private static void opcion1_horasAutomatico(Screen screen, TextGraphics tg) throws IOException {
        int headerRow = MENU_START_ROW + 6;
        writeLines(screen, tg, headerRow, List.of("-------- HORAS AUTOMÁTICAS -----------"));

        int cantidadRow = headerRow + 2;
        tg.putString(1, cantidadRow, "¿Cuántos tiempos deseas sumar? (2 a 6): ");
        screen.refresh();

        // Intento 1: leer con Lanterna (timeout corto)
        String cantidadStr = readLineWithTimeout(screen, tg, cantidadRow, 38, 6);

        // Intento 2: si no llegó, intentar un solo carácter (sin Enter)
        if (cantidadStr == null || cantidadStr.trim().isEmpty()) {
            String single = readSingleCharWithTimeout(screen, tg, cantidadRow, 38, 3);
            if (single != null) cantidadStr = single;
        }

        // Intento 3 (fallback modal Swing)
        if (cantidadStr == null || cantidadStr.trim().isEmpty()) {
            String resp = JOptionPane.showInputDialog(null, "¿Cuántos tiempos deseas sumar? (2 a 6):", "Entrada cantidad", JOptionPane.QUESTION_MESSAGE);
            if (resp == null) {
                writeLines(screen, tg, cantidadRow + 2, List.of("Operación cancelada por el usuario."));
                return;
            }
            cantidadStr = resp.trim();
        }

        writeLines(screen, tg, cantidadRow + 2, List.of("DEBUG: cantidad recibida = " + cantidadStr));

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
        } catch (Exception e) {
            writeLines(screen, tg, cantidadRow + 4, List.of("Cantidad inválida. Debe ser un número."));
            return;
        }
        if (cantidad < 2 || cantidad > 6) {
            writeLines(screen, tg, cantidadRow + 4, List.of("Cantidad inválida. Debe estar entre 2 y 6."));
            return;
        }

        // mostrar los tiempos capturados
        TiempoBinario[] tiempos = new TiempoBinario[cantidad];
        int listStart = cantidadRow + 6;
        for (int i = 0; i < cantidad; i++) {
            int[] hms = reloj.getHoraActual();
            String hBin = conversor.decimalABase(hms[0], 2);
            String mBin = conversor.decimalABase(hms[1], 2);
            String sBin = conversor.decimalABase(hms[2], 2);
            tiempos[i] = new TiempoBinario(hBin, mBin, sBin);

            writeLines(screen, tg, listStart + i, List.of(
                    String.format("Tiempo %d -> %02d:%02d:%02d (binario: %s:%s:%s)", i+1, hms[0], hms[1], hms[2], hBin, mBin, sBin)
            ));
            // pequeña espera para que se note la captura automática
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }

        int promptRow = listStart + cantidad + 1;
        tg.putString(1, promptRow, "¿Desea realizar una Suma (S) o una Resta (R)? ");
        screen.refresh();

        // Intento 1: leer línea completa (con timeout)
        String opcion = readLineWithTimeout(screen, tg, promptRow, 44, 6);

        // Intento 2: si no se recibió línea, intentamos leer un solo carácter (sin Enter)
        if (opcion == null || opcion.trim().isEmpty()) {
            String single = readSingleCharWithTimeout(screen, tg, promptRow, 44, 3);
            if (single != null) opcion = single;
        }

        // Intento 3: fallback modal Swing
        if (opcion == null || opcion.trim().isEmpty()) {
            String resp = JOptionPane.showInputDialog(null, "¿Desea realizar una Suma (S) o una Resta (R)? (S/R)", "Entrada S/R", JOptionPane.QUESTION_MESSAGE);
            if (resp == null) {
                writeLines(screen, tg, promptRow + 2, List.of("Operación cancelada por el usuario."));
                return;
            }
            opcion = resp.trim();
        }

        opcion = opcion.trim().toUpperCase();
        writeLines(screen, tg, promptRow + 2, List.of("DEBUG: opcion recibida = " + opcion));

        int resultadoSegundos = binarioATotalSegundos(tiempos[0]);
        for (int i = 1; i < cantidad; i++) {
            int currentSegundos = binarioATotalSegundos(tiempos[i]);
            if ("S".equals(opcion)) resultadoSegundos += currentSegundos;
            else if ("R".equals(opcion)) resultadoSegundos -= currentSegundos;
            else {
                writeLines(screen, tg, promptRow + 4, List.of("Opción no válida. Ingresa S o R."));
                return;
            }
        }

        // Mostrar resultado final (pantalla y cuadro modal)
        TiempoBinario resultado = segundosATiempoBinario(resultadoSegundos);
        int hDec = Integer.parseInt(resultado.hora, 2);
        int mDec = Integer.parseInt(resultado.minuto, 2);
        int sDec = Integer.parseInt(resultado.segundo, 2);

        List<String> out = new ArrayList<>();
        out.add("====================================");
        out.add("Resultado final:");
        out.add(String.format("Binario: %s:%s:%s", resultado.hora, resultado.minuto, resultado.segundo));
        out.add(String.format("Decimal: %02d:%02d:%02d", hDec, mDec, sDec));
        out.add("====================================");
        writeLines(screen, tg, promptRow + 4, out);

        // Mostrar resultado también en un dialog para asegurar visibilidad
        StringBuilder popup = new StringBuilder();
        popup.append("Resultado final:\n");
        popup.append(String.format("Binario: %s:%s:%s\n", resultado.hora, resultado.minuto, resultado.segundo));
        popup.append(String.format("Decimal: %02d:%02d:%02d\n", hDec, mDec, sDec));
        JOptionPane.showMessageDialog(null, popup.toString(), "Resultado", JOptionPane.INFORMATION_MESSAGE);

        // Pregunta si desea salir o volver al menú
        int sel = JOptionPane.showConfirmDialog(null, "¿Deseas salir del programa? (Sí = salir, No = volver al menú)", "Salir?", JOptionPane.YES_NO_OPTION);
        if (sel == JOptionPane.YES_OPTION) {
            // cerrar pantalla y salir
            try { screen.stopScreen(); } catch (IOException ignored) {}
            System.exit(0);
        }
        // si NO, volvemos al menú (simplemente regresamos)
    }

    // ========== OPCIÓN 2 (horas manuales) con fallback JOptionPane ==========
    private static void opcion2_horasManuales(Screen screen, TextGraphics tg) throws IOException {
        int headerRow = MENU_START_ROW + 6;
        writeLines(screen, tg, headerRow, List.of("--- OPCIÓN 2: INGRESAR TIEMPOS MANUALMENTE ---"));

        int cantidadRow = headerRow + 2;
        tg.putString(1, cantidadRow, "¿Cuántos tiempos deseas sumar? (2 a 6): ");
        screen.refresh();

        // Intento 1: Lanterna
        String cantidadStr = readLineWithTimeout(screen, tg, cantidadRow, 38, 6);
        // Intento 2: single char
        if (cantidadStr == null || cantidadStr.trim().isEmpty()) {
            String single = readSingleCharWithTimeout(screen, tg, cantidadRow, 38, 3);
            if (single != null) cantidadStr = single;
        }
        // Intento 3: fallback Swing
        if (cantidadStr == null || cantidadStr.trim().isEmpty()) {
            String resp = JOptionPane.showInputDialog(null, "¿Cuántos tiempos deseas sumar? (2 a 6):", "Entrada cantidad", JOptionPane.QUESTION_MESSAGE);
            if (resp == null) { writeLines(screen, tg, cantidadRow + 2, List.of("Operación cancelada por el usuario.")); return; }
            cantidadStr = resp.trim();
        }

        int cantidad;
        try { cantidad = Integer.parseInt(cantidadStr); }
        catch (Exception e) { writeLines(screen, tg, cantidadRow + 2, List.of("Cantidad inválida.")); return; }
        if (cantidad < 2 || cantidad > 6) { writeLines(screen, tg, cantidadRow + 2, List.of("Cantidad inválida. Debe estar entre 2 y 6.")); return; }

        TiempoBinario[] tiempos = new TiempoBinario[cantidad];
        int rowCursor = cantidadRow + 4;
        for (int i = 0; i < cantidad; i++) {
            writeLines(screen, tg, rowCursor, List.of(String.format("Ingrese la hora %d en binario (ej: 1010): ", i+1)));
            String h = readLineWithTimeout(screen, tg, rowCursor, 40, 10);
            if (h == null || h.trim().isEmpty()) {
                String resp = JOptionPane.showInputDialog(null, "Ingrese la hora " + (i+1) + " en binario (ej: 1010):", "Hora binaria", JOptionPane.QUESTION_MESSAGE);
                if (resp == null) { writeLines(screen, tg, rowCursor + 2, List.of("Operación cancelada.")); return; }
                h = resp.trim();
            }

            writeLines(screen, tg, rowCursor + 1, List.of(String.format("Ingrese el minuto %d en binario: ", i+1)));
            String m = readLineWithTimeout(screen, tg, rowCursor + 1, 40, 10);
            if (m == null || m.trim().isEmpty()) {
                String resp = JOptionPane.showInputDialog(null, "Ingrese el minuto " + (i+1) + " en binario:", "Minuto binario", JOptionPane.QUESTION_MESSAGE);
                if (resp == null) { writeLines(screen, tg, rowCursor + 2, List.of("Operación cancelada.")); return; }
                m = resp.trim();
            }

            writeLines(screen, tg, rowCursor + 2, List.of(String.format("Ingrese el segundo %d en binario: ", i+1)));
            String s = readLineWithTimeout(screen, tg, rowCursor + 2, 40, 10);
            if (s == null || s.trim().isEmpty()) {
                String resp = JOptionPane.showInputDialog(null, "Ingrese el segundo " + (i+1) + " en binario:", "Segundo binario", JOptionPane.QUESTION_MESSAGE);
                if (resp == null) { writeLines(screen, tg, rowCursor + 2, List.of("Operación cancelada.")); return; }
                s = resp.trim();
            }

            tiempos[i] = new TiempoBinario(h, m, s);
            rowCursor += 4;
        }

        int promptRow = rowCursor;
        tg.putString(1, promptRow, "¿Desea realizar una Suma (S) o una Resta (R)? ");
        screen.refresh();

        String opcion = readLineWithTimeout(screen, tg, promptRow, 44, 6);
        if (opcion == null || opcion.trim().isEmpty()) {
            String single = readSingleCharWithTimeout(screen, tg, promptRow, 44, 3);
            if (single != null) opcion = single;
        }
        if (opcion == null || opcion.trim().isEmpty()) {
            String resp = JOptionPane.showInputDialog(null, "¿Desea realizar una Suma (S) o una Resta (R)? (S/R)", "Entrada S/R", JOptionPane.QUESTION_MESSAGE);
            if (resp == null) { writeLines(screen, tg, promptRow + 2, List.of("Operación cancelada.")); return; }
            opcion = resp.trim();
        }

        opcion = opcion.toUpperCase();
        writeLines(screen, tg, promptRow + 2, List.of("DEBUG: opcion recibida = " + opcion));

        int resultadoSegundos = binarioATotalSegundos(tiempos[0]);
        for (int i = 1; i < cantidad; i++) {
            int currentSegundos = binarioATotalSegundos(tiempos[i]);
            if ("S".equals(opcion)) resultadoSegundos += currentSegundos;
            else if ("R".equals(opcion)) resultadoSegundos -= currentSegundos;
            else { writeLines(screen, tg, promptRow + 4, List.of("Opción no válida.")); return; }
        }

        TiempoBinario resultado = segundosATiempoBinario(resultadoSegundos);
        int hDec = Integer.parseInt(resultado.hora, 2);
        int mDec = Integer.parseInt(resultado.minuto, 2);
        int sDec = Integer.parseInt(resultado.segundo, 2);

        List<String> out = new ArrayList<>();
        out.add("====================================");
        out.add("Resultado final:");
        out.add(String.format("Binario: %s:%s:%s", resultado.hora, resultado.minuto, resultado.segundo));
        out.add(String.format("Decimal: %02d:%02d:%02d", hDec, mDec, sDec));
        out.add("====================================");
        writeLines(screen, tg, promptRow + 4, out);

        // Mostrar resultado también en un dialog para asegurar visibilidad
        StringBuilder popup = new StringBuilder();
        popup.append("Resultado final:\n");
        popup.append(String.format("Binario: %s:%s:%s\n", resultado.hora, resultado.minuto, resultado.segundo));
        popup.append(String.format("Decimal: %02d:%02d:%02d\n", hDec, mDec, sDec));
        JOptionPane.showMessageDialog(null, popup.toString(), "Resultado", JOptionPane.INFORMATION_MESSAGE);

        int sel = JOptionPane.showConfirmDialog(null, "¿Deseas salir del programa? (Sí = salir, No = volver al menú)", "Salir?", JOptionPane.YES_NO_OPTION);
        if (sel == JOptionPane.YES_OPTION) {
            try { screen.stopScreen(); } catch (IOException ignored) {}
            System.exit(0);
        }
    }

    public static void main(String[] args) throws IOException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        Screen screen = factory.createScreen();
        screen.startScreen();
        screen.doResizeIfNecessary();

        TextGraphics tg = screen.newTextGraphics();

        // Dibuja hora inicialmente y planifica actualización cada 1s
        drawClock(tg);
        drawMenu(tg);
        screen.refresh();

        ScheduledFuture<?> updater = scheduler.scheduleAtFixedRate(() -> {
            try {
                drawClock(tg);
                screen.refresh();
            } catch (IOException ignored) { }
        }, 1, 1, TimeUnit.SECONDS);

        try {
            boolean running = true;
            drawMenu(tg);
            screen.refresh();

            while (running) {
                KeyStroke key = screen.pollInput(); // no bloqueante
                if (key == null) {
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                    continue;
                }

                switch (key.getKeyType()) {
                    case Character:
                        char c = key.getCharacter();
                        if (c == '1') {
                            opcion1_horasAutomatico(screen, tg);
                            drawMenu(tg);
                        } else if (c == '2') {
                            opcion2_horasManuales(screen, tg);
                            drawMenu(tg);
                        } else if (c == '3') {
                            running = false;
                        }
                        break;
                    case EOF:
                        running = false;
                        break;
                    default:
                        // ignorar otras teclas
                }
                screen.refresh();
            }
        } finally {
            updater.cancel(true);
            scheduler.shutdownNow();
            screen.stopScreen();
        }
    }
}
