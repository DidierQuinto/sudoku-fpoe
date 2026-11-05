package co.edu.univalle.sudoku.modelo;

/**
 * Resultado de intentar escribir o borrar un número en el tablero.
 * El controlador lo usa para decidir qué mensaje mostrarle al jugador.
 */
public enum ResultadoJugada {
    /** La jugada se aplicó y no genera conflicto. */
    OK,
    /** La jugada se aplicó pero el número se repite en su fila, columna o bloque. */
    CON_CONFLICTO,
    /** La celda es parte del tablero inicial y no se puede modificar. */
    CELDA_FIJA,
    /** El número no está entre 1 y 6. */
    VALOR_INVALIDO,
    /** Se intentó borrar una celda que ya estaba vacía. */
    YA_VACIA,
    /** No hay un juego en curso. */
    SIN_JUEGO
}
