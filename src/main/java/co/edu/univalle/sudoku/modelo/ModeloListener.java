package co.edu.univalle.sudoku.modelo;

import java.util.Set;

/**
 * Interfaz oyente (patrón Observer) mediante la cual el modelo avisa a quien le interese
 * que algo cambió, sin conocer la interfaz gráfica.
 * <p>
 * Como casi nunca se necesitan todos los métodos, existe la clase adaptadora
 * {@link ModeloAdapter} con implementaciones vacías.
 */
public interface ModeloListener {

    /**
     * Se inició un juego nuevo.
     *
     * @param tableroInicial copia del tablero inicial (con sus celdas fijas)
     */
    void onJuegoIniciado(Tablero tableroInicial);

    /**
     * El jugador escribió o borró un número.
     *
     * @param posicion celda modificada
     * @param valor    nuevo valor (0 si se borró)
     */
    void onCeldaCambiada(Posicion posicion, int valor);

    /**
     * Cambió el conjunto de celdas con entradas incorrectas.
     *
     * @param conflictos celdas del jugador cuyo número se repite (puede estar vacío)
     */
    void onConflictosActualizados(Set<Posicion> conflictos);

    /**
     * Se generó una sugerencia de ayuda.
     *
     * @param posicion celda sugerida
     * @param valor    número sugerido
     */
    void onSugerencia(Posicion posicion, int valor);

    /** El jugador completó el tablero sin errores. */
    void onJuegoCompletado();
}
