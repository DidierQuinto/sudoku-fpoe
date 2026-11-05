package co.edu.univalle.sudoku.modelo;

/**
 * Coordenada de una celda del tablero. Los índices comienzan en 0.
 * <p>
 * Al ser un {@code record} es inmutable y ya trae {@code equals}/{@code hashCode},
 * por lo que se puede usar directamente dentro de un {@link java.util.Set}.
 *
 * @param fila    índice de la fila (0 a 5)
 * @param columna índice de la columna (0 a 5)
 */
public record Posicion(int fila, int columna) {

    /**
     * Valida que la coordenada esté dentro del tablero.
     *
     * @throws IllegalArgumentException si la fila o la columna están fuera de rango
     */
    public Posicion {
        if (fila < 0 || fila >= Tablero.TAMANO || columna < 0 || columna >= Tablero.TAMANO) {
            throw new IllegalArgumentException("Posición fuera del tablero: (" + fila + ", " + columna + ")");
        }
    }

    /**
     * Índice del bloque de 2x3 al que pertenece la celda (0 a 5, de izquierda a derecha
     * y de arriba hacia abajo).
     *
     * @return índice del bloque
     */
    public int bloque() {
        return Tablero.indiceBloque(fila, columna);
    }
}
