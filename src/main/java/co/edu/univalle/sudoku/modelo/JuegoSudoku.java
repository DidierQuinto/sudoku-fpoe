package co.edu.univalle.sudoku.modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Lógica de una partida de Sudoku 6x6: inicio del juego, ingreso y borrado de números,
 * validación en tiempo real y ayuda limitada.
 * <p>
 * Es la «fachada» del modelo: el controlador solo habla con esta clase. Los cambios se
 * comunican a los {@link ModeloListener} registrados.
 */
public class JuegoSudoku {

    /** Cantidad máxima de ayudas por partida. */
    public static final int MAX_AYUDAS = 3;

    private final GeneradorTablero generador;
    private final List<ModeloListener> oyentes = new ArrayList<>();

    private Tablero tablero = new Tablero();
    private Tablero solucion = new Tablero();
    private int ayudasRestantes = MAX_AYUDAS;
    private boolean enCurso = false;

    /** Crea un juego con un generador de tableros aleatorio. */
    public JuegoSudoku() {
        this(new GeneradorTablero());
    }

    /**
     * Crea un juego con el generador indicado.
     *
     * @param generador generador de tableros
     */
    public JuegoSudoku(GeneradorTablero generador) {
        this.generador = generador;
    }

    /**
     * Registra un oyente que será avisado de los cambios del juego.
     *
     * @param oyente oyente a registrar
     */
    public void agregarListener(ModeloListener oyente) {
        oyentes.add(oyente);
    }

    /**
     * Inicia una partida nueva (HU-2): genera una solución, deja 2 números en cada bloque
     * de 2x3, verifica que el tablero inicial sea válido y reinicia las ayudas.
     *
     * @throws IllegalStateException si el tablero generado no fuera válido (no debería pasar)
     */
    public void iniciarJuego() {
        solucion = generador.generarSolucion();
        tablero = generador.generarTableroInicial(solucion);
        int esperadas = Tablero.TAMANO * Tablero.TAMANO
                - GeneradorTablero.NUMEROS_POR_BLOQUE * (Tablero.TAMANO * Tablero.TAMANO
                / (Tablero.ALTO_BLOQUE * Tablero.ANCHO_BLOQUE));
        if (!tablero.calcularConflictos().isEmpty() || tablero.contarVacias() != esperadas) {
            throw new IllegalStateException("El tablero inicial generado no es válido");
        }
        ayudasRestantes = MAX_AYUDAS;
        enCurso = true;
        notificar(oyente -> oyente.onJuegoIniciado(tablero.copia()));
    }

    /**
     * Escribe un número en una celda (HU-3) y valida el tablero en tiempo real (HU-4).
     *
     * @param posicion celda elegida
     * @param valor    número de 1 a 6
     * @return qué pasó con la jugada
     */
    public ResultadoJugada ingresarNumero(Posicion posicion, int valor) {
        if (!enCurso) {
            return ResultadoJugada.SIN_JUEGO;
        }
        if (valor < 1 || valor > Tablero.TAMANO) {
            return ResultadoJugada.VALOR_INVALIDO;
        }
        if (tablero.esFija(posicion.fila(), posicion.columna())) {
            return ResultadoJugada.CELDA_FIJA;
        }
        tablero.setValor(posicion.fila(), posicion.columna(), valor);
        return despuesDeCambio(posicion, valor);
    }

    /**
     * Borra el número de una celda (HU-3).
     *
     * @param posicion celda elegida
     * @return qué pasó con la jugada
     */
    public ResultadoJugada borrarNumero(Posicion posicion) {
        if (!enCurso) {
            return ResultadoJugada.SIN_JUEGO;
        }
        if (tablero.esFija(posicion.fila(), posicion.columna())) {
            return ResultadoJugada.CELDA_FIJA;
        }
        if (tablero.estaVacia(posicion.fila(), posicion.columna())) {
            return ResultadoJugada.YA_VACIA;
        }
        tablero.setValor(posicion.fila(), posicion.columna(), Tablero.VACIO);
        return despuesDeCambio(posicion, Tablero.VACIO);
    }

    /**
     * Pide una sugerencia (HU-5). Solo <em>sugiere</em> un número: NO lo escribe en el tablero,
     * así que el jugador debe escribirlo él mismo y nunca se puede terminar el juego solo con
     * la ayuda. Además hay un máximo de {@link #MAX_AYUDAS} ayudas por partida.
     * <p>
     * Si la celda indicada está vacía se sugiere para esa celda; si no, se escoge la celda
     * vacía con menos opciones posibles.
     *
     * @param preferida celda seleccionada por el jugador (puede ser {@code null})
     * @return la sugerencia, o el motivo por el que no se pudo dar
     */
    public ResultadoAyuda pedirAyuda(Posicion preferida) {
        if (!enCurso) {
            return ResultadoAyuda.fallo(ResultadoAyuda.Estado.SIN_JUEGO);
        }
        if (ayudasRestantes <= 0) {
            return ResultadoAyuda.fallo(ResultadoAyuda.Estado.SIN_AYUDAS);
        }
        if (!getConflictos().isEmpty()) {
            return ResultadoAyuda.fallo(ResultadoAyuda.Estado.CON_ERRORES);
        }
        Posicion destino = elegirCeldaVacia(preferida);
        if (destino == null) {
            return ResultadoAyuda.fallo(ResultadoAyuda.Estado.SIN_CELDAS_VACIAS);
        }
        List<Integer> candidatos = tablero.candidatos(destino.fila(), destino.columna());
        if (candidatos.isEmpty()) {
            return ResultadoAyuda.fallo(ResultadoAyuda.Estado.SIN_OPCIONES);
        }
        int deLaSolucion = solucion.getValor(destino.fila(), destino.columna());
        int sugerido = candidatos.contains(deLaSolucion) ? deLaSolucion : candidatos.get(0);
        ayudasRestantes--;
        notificar(oyente -> oyente.onSugerencia(destino, sugerido));
        return new ResultadoAyuda(ResultadoAyuda.Estado.OK, destino, sugerido);
    }

    /**
     * Celdas escritas por el jugador cuyo número se repite en su fila, columna o bloque.
     *
     * @return conjunto de posiciones incorrectas (vacío si todo va bien)
     */
    public Set<Posicion> getConflictos() {
        Set<Posicion> delJugador = new HashSet<>();
        for (Posicion p : tablero.calcularConflictos()) {
            if (!tablero.esFija(p.fila(), p.columna())) {
                delJugador.add(p);
            }
        }
        return delJugador;
    }

    /**
     * Mensaje que explica por qué el número de la celda está mal.
     *
     * @param posicion celda a explicar
     * @return mensaje, o {@code null} si no hay conflicto
     */
    public String describirConflicto(Posicion posicion) {
        return tablero.describirConflicto(posicion.fila(), posicion.columna());
    }

    /**
     * Indica si la celda es parte del tablero inicial.
     *
     * @param posicion celda a consultar
     * @return {@code true} si es fija
     */
    public boolean esFija(Posicion posicion) {
        return tablero.esFija(posicion.fila(), posicion.columna());
    }

    /**
     * Cantidad de celdas sin número.
     *
     * @return celdas vacías
     */
    public int getCeldasVacias() {
        return tablero.contarVacias();
    }

    /**
     * Ayudas que todavía puede pedir el jugador.
     *
     * @return ayudas restantes
     */
    public int getAyudasRestantes() {
        return ayudasRestantes;
    }

    /**
     * Indica si hay una partida en curso.
     *
     * @return {@code true} si se inició un juego y aún no se ha completado
     */
    public boolean isEnCurso() {
        return enCurso;
    }

    private ResultadoJugada despuesDeCambio(Posicion posicion, int valor) {
        Set<Posicion> conflictos = getConflictos();
        if (conflictos.isEmpty() && tablero.estaLleno()) {
            enCurso = false;
        }
        notificar(oyente -> oyente.onCeldaCambiada(posicion, valor));
        notificar(oyente -> oyente.onConflictosActualizados(conflictos));
        if (!enCurso) {
            notificar(ModeloListener::onJuegoCompletado);
        }
        return conflictos.contains(posicion) ? ResultadoJugada.CON_CONFLICTO : ResultadoJugada.OK;
    }

    private Posicion elegirCeldaVacia(Posicion preferida) {
        if (preferida != null && tablero.estaVacia(preferida.fila(), preferida.columna())) {
            return preferida;
        }
        Posicion mejor = null;
        int menosOpciones = Integer.MAX_VALUE;
        for (int fila = 0; fila < Tablero.TAMANO; fila++) {
            for (int columna = 0; columna < Tablero.TAMANO; columna++) {
                if (tablero.estaVacia(fila, columna)) {
                    int opciones = tablero.candidatos(fila, columna).size();
                    if (opciones < menosOpciones) {
                        menosOpciones = opciones;
                        mejor = new Posicion(fila, columna);
                    }
                }
            }
        }
        return mejor;
    }

    private void notificar(Consumer<ModeloListener> accion) {
        for (ModeloListener oyente : new ArrayList<>(oyentes)) {
            accion.accept(oyente);
        }
    }
}
