package co.edu.univalle.sudoku.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Crea los tableros de cada partida (HU-2).
 * <p>
 * Primero construye una solución completa y válida usando backtracking con orden
 * aleatorio; después deja visibles únicamente 2 números en cada bloque de 2x3
 * (12 celdas fijas en total), tomados de esa solución. Así el tablero inicial
 * siempre es válido y siempre tiene solución.
 */
public class GeneradorTablero {

    /** Cantidad de números visibles al inicio en cada bloque de 2x3. */
    public static final int NUMEROS_POR_BLOQUE = 2;

    private final Random aleatorio;

    /** Crea un generador con semilla aleatoria. */
    public GeneradorTablero() {
        this(new Random());
    }

    /**
     * Crea un generador con una fuente de azar dada (útil para pruebas reproducibles).
     *
     * @param aleatorio fuente de números aleatorios
     */
    public GeneradorTablero(Random aleatorio) {
        this.aleatorio = aleatorio;
    }

    /**
     * Genera un tablero 6x6 completamente lleno que cumple las reglas del Sudoku.
     *
     * @return solución completa
     */
    public Tablero generarSolucion() {
        Tablero tablero = new Tablero();
        if (!rellenar(tablero, 0)) {
            throw new IllegalStateException("No se pudo generar una solución de Sudoku");
        }
        return tablero;
    }

    /**
     * Crea el tablero inicial de la partida: en cada bloque de 2x3 deja 2 números
     * (elegidos al azar) copiados de la solución y los marca como fijos.
     *
     * @param solucion tablero completo del que se toman los números
     * @return tablero inicial con 12 celdas fijas
     */
    public Tablero generarTableroInicial(Tablero solucion) {
        Tablero inicial = new Tablero();
        int bloquesPorFila = Tablero.TAMANO / Tablero.ANCHO_BLOQUE;
        int totalBloques = bloquesPorFila * (Tablero.TAMANO / Tablero.ALTO_BLOQUE);
        for (int bloque = 0; bloque < totalBloques; bloque++) {
            int fila0 = (bloque / bloquesPorFila) * Tablero.ALTO_BLOQUE;
            int columna0 = (bloque % bloquesPorFila) * Tablero.ANCHO_BLOQUE;
            List<Posicion> celdasDelBloque = new ArrayList<>();
            for (int i = fila0; i < fila0 + Tablero.ALTO_BLOQUE; i++) {
                for (int j = columna0; j < columna0 + Tablero.ANCHO_BLOQUE; j++) {
                    celdasDelBloque.add(new Posicion(i, j));
                }
            }
            Collections.shuffle(celdasDelBloque, aleatorio);
            for (int k = 0; k < NUMEROS_POR_BLOQUE; k++) {
                Posicion p = celdasDelBloque.get(k);
                inicial.fijarValor(p.fila(), p.columna(), solucion.getValor(p.fila(), p.columna()));
            }
        }
        return inicial;
    }

    /** Backtracking: llena las celdas en orden, probando los candidatos en orden aleatorio. */
    private boolean rellenar(Tablero tablero, int indice) {
        if (indice == Tablero.TAMANO * Tablero.TAMANO) {
            return true;
        }
        int fila = indice / Tablero.TAMANO;
        int columna = indice % Tablero.TAMANO;
        List<Integer> candidatos = tablero.candidatos(fila, columna);
        Collections.shuffle(candidatos, aleatorio);
        for (int valor : candidatos) {
            tablero.setValor(fila, columna, valor);
            if (rellenar(tablero, indice + 1)) {
                return true;
            }
            tablero.setValor(fila, columna, Tablero.VACIO);
        }
        return false;
    }
}
