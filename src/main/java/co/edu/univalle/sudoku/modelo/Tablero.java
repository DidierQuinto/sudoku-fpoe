package co.edu.univalle.sudoku.modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cuadrícula de Sudoku de 6x6 dividida en 6 bloques de 2 filas por 3 columnas.
 * <p>
 * Estructuras de datos usadas: una matriz {@code int[6][6]} con los valores (0 = vacía),
 * una matriz {@code boolean[6][6]} que marca las celdas fijas del tablero inicial,
 * {@link List} para los candidatos y {@link Set} para los conflictos.
 * <p>
 * Esta clase no conoce nada de la interfaz gráfica: solo contiene las reglas del juego.
 */
public class Tablero {

    /** Número de filas y de columnas. */
    public static final int TAMANO = 6;
    /** Alto de cada bloque (filas). */
    public static final int ALTO_BLOQUE = 2;
    /** Ancho de cada bloque (columnas). */
    public static final int ANCHO_BLOQUE = 3;
    /** Valor que representa una celda vacía. */
    public static final int VACIO = 0;

    private final int[][] valores = new int[TAMANO][TAMANO];
    private final boolean[][] fijas = new boolean[TAMANO][TAMANO];

    /**
     * Devuelve el valor de una celda.
     *
     * @param fila    fila (0 a 5)
     * @param columna columna (0 a 5)
     * @return valor de 1 a 6, o {@link #VACIO} si la celda está vacía
     */
    public int getValor(int fila, int columna) {
        validarIndices(fila, columna);
        return valores[fila][columna];
    }

    /**
     * Indica si la celda pertenece al tablero inicial (no la puede modificar el jugador).
     *
     * @param fila    fila (0 a 5)
     * @param columna columna (0 a 5)
     * @return {@code true} si la celda es fija
     */
    public boolean esFija(int fila, int columna) {
        validarIndices(fila, columna);
        return fijas[fila][columna];
    }

    /**
     * Indica si la celda está vacía.
     *
     * @param fila    fila (0 a 5)
     * @param columna columna (0 a 5)
     * @return {@code true} si no tiene número
     */
    public boolean estaVacia(int fila, int columna) {
        return getValor(fila, columna) == VACIO;
    }

    /**
     * Asigna un valor a una celda sin comprobar las reglas del Sudoku
     * (la validación de reglas se hace con {@link #calcularConflictos()}).
     *
     * @param fila    fila (0 a 5)
     * @param columna columna (0 a 5)
     * @param valor   número de 1 a 6, o {@link #VACIO} para borrar
     * @throws IllegalArgumentException si el valor no está entre 0 y 6
     */
    public void setValor(int fila, int columna, int valor) {
        validarIndices(fila, columna);
        if (valor < VACIO || valor > TAMANO) {
            throw new IllegalArgumentException("Valor fuera de rango (0-" + TAMANO + "): " + valor);
        }
        valores[fila][columna] = valor;
    }

    /**
     * Asigna un valor y lo marca como celda fija del tablero inicial.
     *
     * @param fila    fila (0 a 5)
     * @param columna columna (0 a 5)
     * @param valor   número de 1 a 6
     */
    public void fijarValor(int fila, int columna, int valor) {
        setValor(fila, columna, valor);
        fijas[fila][columna] = valor != VACIO;
    }

    /**
     * Calcula el índice del bloque de 2x3 que contiene la celda.
     * Los bloques se numeran de 0 a 5, de izquierda a derecha y de arriba hacia abajo.
     *
     * @param fila    fila (0 a 5)
     * @param columna columna (0 a 5)
     * @return índice del bloque (0 a 5)
     */
    public static int indiceBloque(int fila, int columna) {
        int bloquesPorFila = TAMANO / ANCHO_BLOQUE;
        return (fila / ALTO_BLOQUE) * bloquesPorFila + (columna / ANCHO_BLOQUE);
    }

    /**
     * Comprueba si {@code valor} NO se repite en la fila, la columna ni el bloque de la celda
     * (la propia celda se ignora).
     *
     * @param fila    fila de la celda
     * @param columna columna de la celda
     * @param valor   número de 1 a 6 que se quiere probar
     * @return {@code true} si colocar ese valor respeta las reglas
     */
    public boolean puedeColocar(int fila, int columna, int valor) {
        validarIndices(fila, columna);
        for (int i = 0; i < TAMANO; i++) {
            if (i != columna && valores[fila][i] == valor) {
                return false;
            }
            if (i != fila && valores[i][columna] == valor) {
                return false;
            }
        }
        int fila0 = (fila / ALTO_BLOQUE) * ALTO_BLOQUE;
        int columna0 = (columna / ANCHO_BLOQUE) * ANCHO_BLOQUE;
        for (int i = fila0; i < fila0 + ALTO_BLOQUE; i++) {
            for (int j = columna0; j < columna0 + ANCHO_BLOQUE; j++) {
                if ((i != fila || j != columna) && valores[i][j] == valor) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Lista los números que se pueden colocar en una celda vacía respetando las reglas.
     *
     * @param fila    fila de la celda
     * @param columna columna de la celda
     * @return lista de candidatos (vacía si la celda ya tiene número o no hay opciones)
     */
    public List<Integer> candidatos(int fila, int columna) {
        List<Integer> resultado = new ArrayList<>();
        if (!estaVacia(fila, columna)) {
            return resultado;
        }
        for (int valor = 1; valor <= TAMANO; valor++) {
            if (puedeColocar(fila, columna, valor)) {
                resultado.add(valor);
            }
        }
        return resultado;
    }

    /**
     * Busca todas las celdas cuyo número se repite en su fila, columna o bloque.
     *
     * @return conjunto de posiciones en conflicto (vacío si el tablero es válido)
     */
    public Set<Posicion> calcularConflictos() {
        Set<Posicion> conflictos = new HashSet<>();
        for (int fila = 0; fila < TAMANO; fila++) {
            for (int columna = 0; columna < TAMANO; columna++) {
                int valor = valores[fila][columna];
                if (valor != VACIO && !puedeColocar(fila, columna, valor)) {
                    conflictos.add(new Posicion(fila, columna));
                }
            }
        }
        return conflictos;
    }

    /**
     * Genera un mensaje que explica dónde se repite el número de una celda,
     * por ejemplo: «El 4 ya está en la fila 2 y en el bloque 3.».
     *
     * @param fila    fila de la celda
     * @param columna columna de la celda
     * @return el mensaje, o {@code null} si la celda está vacía o no tiene conflicto
     */
    public String describirConflicto(int fila, int columna) {
        validarIndices(fila, columna);
        int valor = valores[fila][columna];
        if (valor == VACIO) {
            return null;
        }
        List<String> lugares = new ArrayList<>();
        for (int j = 0; j < TAMANO; j++) {
            if (j != columna && valores[fila][j] == valor) {
                lugares.add("en la fila " + (fila + 1));
                break;
            }
        }
        for (int i = 0; i < TAMANO; i++) {
            if (i != fila && valores[i][columna] == valor) {
                lugares.add("en la columna " + (columna + 1));
                break;
            }
        }
        int fila0 = (fila / ALTO_BLOQUE) * ALTO_BLOQUE;
        int columna0 = (columna / ANCHO_BLOQUE) * ANCHO_BLOQUE;
        boolean enBloque = false;
        for (int i = fila0; i < fila0 + ALTO_BLOQUE && !enBloque; i++) {
            for (int j = columna0; j < columna0 + ANCHO_BLOQUE; j++) {
                if ((i != fila || j != columna) && valores[i][j] == valor) {
                    enBloque = true;
                    break;
                }
            }
        }
        if (enBloque) {
            lugares.add("en el bloque " + (indiceBloque(fila, columna) + 1));
        }
        if (lugares.isEmpty()) {
            return null;
        }
        return "El " + valor + " ya está " + unir(lugares) + ".";
    }

    /**
     * Cuenta las celdas vacías.
     *
     * @return cantidad de celdas sin número
     */
    public int contarVacias() {
        int vacias = 0;
        for (int[] fila : valores) {
            for (int valor : fila) {
                if (valor == VACIO) {
                    vacias++;
                }
            }
        }
        return vacias;
    }

    /**
     * Indica si todas las celdas tienen número.
     *
     * @return {@code true} si no queda ninguna celda vacía
     */
    public boolean estaLleno() {
        return contarVacias() == 0;
    }

    /**
     * Crea una copia independiente del tablero (valores y celdas fijas).
     *
     * @return copia del tablero
     */
    public Tablero copia() {
        Tablero copia = new Tablero();
        for (int fila = 0; fila < TAMANO; fila++) {
            for (int columna = 0; columna < TAMANO; columna++) {
                copia.valores[fila][columna] = valores[fila][columna];
                copia.fijas[fila][columna] = fijas[fila][columna];
            }
        }
        return copia;
    }

    /**
     * Representación en texto del tablero (útil para depurar en consola).
     *
     * @return el tablero en 6 líneas, con «.» en las celdas vacías
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int fila = 0; fila < TAMANO; fila++) {
            for (int columna = 0; columna < TAMANO; columna++) {
                int valor = valores[fila][columna];
                sb.append(valor == VACIO ? "." : String.valueOf(valor));
                sb.append(columna == TAMANO - 1 ? "\n" : " ");
            }
        }
        return sb.toString();
    }

    private static String unir(List<String> partes) {
        if (partes.size() == 1) {
            return partes.get(0);
        }
        String ultima = partes.get(partes.size() - 1);
        return String.join(", ", partes.subList(0, partes.size() - 1)) + " y " + ultima;
    }

    private static void validarIndices(int fila, int columna) {
        if (fila < 0 || fila >= TAMANO || columna < 0 || columna >= TAMANO) {
            throw new IllegalArgumentException("Posición fuera del tablero: (" + fila + ", " + columna + ")");
        }
    }
}
