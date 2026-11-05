package co.edu.univalle.sudoku.modelo;

/**
 * Resultado de pedir una ayuda (HU-5).
 *
 * @param estado   indica si se pudo dar la sugerencia o por qué no
 * @param posicion celda sugerida (solo si el estado es {@link Estado#OK}, si no es {@code null})
 * @param valor    número sugerido para esa celda (solo si el estado es {@link Estado#OK})
 */
public record ResultadoAyuda(Estado estado, Posicion posicion, int valor) {

    /** Posibles resultados de pedir una ayuda. */
    public enum Estado {
        /** Se generó una sugerencia. */
        OK,
        /** No hay un juego en curso. */
        SIN_JUEGO,
        /** El jugador ya usó todas las ayudas permitidas. */
        SIN_AYUDAS,
        /** Hay números repetidos en el tablero; primero se deben corregir. */
        CON_ERRORES,
        /** La celda elegida no tiene ningún número posible. */
        SIN_OPCIONES,
        /** No quedan celdas vacías. */
        SIN_CELDAS_VACIAS
    }

    /**
     * Crea un resultado fallido (sin sugerencia).
     *
     * @param estado motivo por el que no se dio la sugerencia
     * @return resultado sin posición ni valor
     */
    static ResultadoAyuda fallo(Estado estado) {
        return new ResultadoAyuda(estado, null, 0);
    }
}
