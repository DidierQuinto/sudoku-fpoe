package co.edu.univalle.sudoku.modelo;

import java.util.Set;

/**
 * Clase adaptadora de {@link ModeloListener}: implementa todos los métodos vacíos para que
 * quien la extienda sobrescriba únicamente los eventos que le interesan.
 */
public class ModeloAdapter implements ModeloListener {

    @Override
    public void onJuegoIniciado(Tablero tableroInicial) {
        // Por defecto no hace nada.
    }

    @Override
    public void onCeldaCambiada(Posicion posicion, int valor) {
        // Por defecto no hace nada.
    }

    @Override
    public void onConflictosActualizados(Set<Posicion> conflictos) {
        // Por defecto no hace nada.
    }

    @Override
    public void onSugerencia(Posicion posicion, int valor) {
        // Por defecto no hace nada.
    }

    @Override
    public void onJuegoCompletado() {
        // Por defecto no hace nada.
    }
}
