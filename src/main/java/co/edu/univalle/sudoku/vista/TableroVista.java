package co.edu.univalle.sudoku.vista;

import co.edu.univalle.sudoku.modelo.Posicion;
import co.edu.univalle.sudoku.modelo.Tablero;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Parte de la vista que dibuja el tablero 6x6 sobre un {@link GridPane} declarado en el FXML.
 * <p>
 * El tablero se arma con layouts anidados: un {@code GridPane} exterior de 3 filas x 2 columnas
 * donde cada casilla es un {@code GridPane} de 2 filas x 3 columnas (un bloque), y cada celda
 * es un {@link Label}. Esta clase solo se ocupa de mostrar; no conoce las reglas del juego.
 * Los eventos del mouse se envían al controlador mediante {@link Consumer}.
 */
public final class TableroVista {

    private static final double TAMANO_CELDA = 66;
    private static final int N = Tablero.TAMANO;

    private static final String CLASE_BASE = "celda";
    private static final String CLASE_FIJA = "celda-fija";
    private static final String CLASE_COMPLETA = "celda-completa";
    private static final String CLASE_CONFLICTO = "celda-conflicto";
    private static final String CLASE_SUGERENCIA = "celda-sugerencia";
    private static final String CLASE_SELECCIONADA = "celda-seleccionada";
    private static final List<String> CLASES_DE_ESTADO =
            List.of(CLASE_FIJA, CLASE_COMPLETA, CLASE_CONFLICTO, CLASE_SUGERENCIA, CLASE_SELECCIONADA);

    private final Label[][] celdas = new Label[N][N];
    private final boolean[][] fijas = new boolean[N][N];
    private Set<Posicion> conflictos = new HashSet<>();
    private Posicion seleccionada;
    private Posicion sugerida;
    private boolean completado;

    /**
     * Construye las 36 celdas dentro del contenedor y muestra un tablero vacío.
     *
     * @param contenedor      {@code GridPane} del FXML donde se dibuja el tablero
     * @param alClicIzquierdo acción cuando el jugador hace clic izquierdo en una celda
     * @param alClicDerecho   acción cuando el jugador hace clic derecho en una celda
     */
    public TableroVista(GridPane contenedor, Consumer<Posicion> alClicIzquierdo, Consumer<Posicion> alClicDerecho) {
        contenedor.getChildren().clear();
        contenedor.getStyleClass().add("tablero");

        int bloquesPorFila = N / Tablero.ANCHO_BLOQUE;
        int bloquesPorColumna = N / Tablero.ALTO_BLOQUE;
        for (int bf = 0; bf < bloquesPorColumna; bf++) {
            for (int bc = 0; bc < bloquesPorFila; bc++) {
                GridPane bloque = new GridPane();
                bloque.getStyleClass().add("bloque");
                for (int r = 0; r < Tablero.ALTO_BLOQUE; r++) {
                    for (int c = 0; c < Tablero.ANCHO_BLOQUE; c++) {
                        int fila = bf * Tablero.ALTO_BLOQUE + r;
                        int columna = bc * Tablero.ANCHO_BLOQUE + c;
                        Label celda = crearCelda(new Posicion(fila, columna), alClicIzquierdo, alClicDerecho);
                        celdas[fila][columna] = celda;
                        bloque.add(celda, c, r);
                    }
                }
                contenedor.add(bloque, bc, bf);
            }
        }
        mostrarTablero(new Tablero());
    }

    /**
     * Dibuja un tablero nuevo: muestra los números, marca las celdas fijas y
     * borra selección, errores y sugerencias anteriores.
     *
     * @param tablero tablero a mostrar
     */
    public void mostrarTablero(Tablero tablero) {
        completado = false;
        conflictos = new HashSet<>();
        seleccionada = null;
        sugerida = null;
        for (int fila = 0; fila < N; fila++) {
            for (int columna = 0; columna < N; columna++) {
                fijas[fila][columna] = tablero.esFija(fila, columna);
                celdas[fila][columna].setText(textoDe(tablero.getValor(fila, columna)));
            }
        }
        refrescarTodo();
    }

    /**
     * Muestra (o borra, si es 0) el número de una celda.
     *
     * @param posicion celda a actualizar
     * @param valor    número de 1 a 6, o 0 para dejarla vacía
     */
    public void mostrarValor(Posicion posicion, int valor) {
        celdas[posicion.fila()][posicion.columna()].setText(textoDe(valor));
        refrescarEstilo(posicion);
    }

    /**
     * Resalta la celda seleccionada y quita el resaltado de la anterior.
     *
     * @param posicion nueva celda seleccionada, o {@code null} para quitar la selección
     */
    public void seleccionar(Posicion posicion) {
        Posicion anterior = seleccionada;
        seleccionada = posicion;
        if (anterior != null) {
            refrescarEstilo(anterior);
        }
        if (posicion != null) {
            refrescarEstilo(posicion);
        }
    }

    /**
     * Resalta con borde rojo las celdas con entradas incorrectas.
     *
     * @param nuevosConflictos celdas en conflicto (reemplaza las anteriores)
     */
    public void marcarConflictos(Set<Posicion> nuevosConflictos) {
        Set<Posicion> afectadas = new HashSet<>(conflictos);
        afectadas.addAll(nuevosConflictos);
        conflictos = new HashSet<>(nuevosConflictos);
        afectadas.forEach(this::refrescarEstilo);
    }

    /**
     * Muestra la sugerencia de ayuda dentro de la celda, destacada en amarillo.
     * No cuenta como número escrito: la celda sigue vacía para el jugador.
     *
     * @param posicion celda sugerida
     * @param valor    número sugerido
     */
    public void mostrarSugerencia(Posicion posicion, int valor) {
        limpiarSugerencia();
        sugerida = posicion;
        celdas[posicion.fila()][posicion.columna()].setText(textoDe(valor));
        refrescarEstilo(posicion);
    }

    /** Quita la sugerencia visible (si hay una) y deja la celda vacía. */
    public void limpiarSugerencia() {
        if (sugerida == null) {
            return;
        }
        Posicion anterior = sugerida;
        sugerida = null;
        celdas[anterior.fila()][anterior.columna()].setText("");
        refrescarEstilo(anterior);
    }

    /** Pinta todo el tablero de verde para indicar que se completó. */
    public void marcarCompletado() {
        completado = true;
        seleccionada = null;
        sugerida = null;
        refrescarTodo();
    }

    private Label crearCelda(Posicion posicion, Consumer<Posicion> alClicIzquierdo, Consumer<Posicion> alClicDerecho) {
        Label celda = new Label();
        celda.getStyleClass().add(CLASE_BASE);
        celda.setMinSize(TAMANO_CELDA, TAMANO_CELDA);
        celda.setPrefSize(TAMANO_CELDA, TAMANO_CELDA);
        celda.setMaxSize(TAMANO_CELDA, TAMANO_CELDA);
        celda.setAlignment(Pos.CENTER);
        celda.setOnMouseClicked(evento -> {
            if (evento.getButton() == MouseButton.PRIMARY) {
                alClicIzquierdo.accept(posicion);
            } else if (evento.getButton() == MouseButton.SECONDARY) {
                alClicDerecho.accept(posicion);
            }
        });
        return celda;
    }

    /** Recalcula las clases CSS de una celda según su estado actual. */
    private void refrescarEstilo(Posicion posicion) {
        Label celda = celdas[posicion.fila()][posicion.columna()];
        celda.getStyleClass().removeAll(CLASES_DE_ESTADO);
        if (fijas[posicion.fila()][posicion.columna()]) {
            celda.getStyleClass().add(CLASE_FIJA);
        }
        if (completado) {
            celda.getStyleClass().add(CLASE_COMPLETA);
        }
        if (conflictos.contains(posicion)) {
            celda.getStyleClass().add(CLASE_CONFLICTO);
        }
        if (posicion.equals(sugerida)) {
            celda.getStyleClass().add(CLASE_SUGERENCIA);
        }
        if (posicion.equals(seleccionada)) {
            celda.getStyleClass().add(CLASE_SELECCIONADA);
        }
    }

    private void refrescarTodo() {
        for (int fila = 0; fila < N; fila++) {
            for (int columna = 0; columna < N; columna++) {
                refrescarEstilo(new Posicion(fila, columna));
            }
        }
    }

    private static String textoDe(int valor) {
        return valor == Tablero.VACIO ? "" : String.valueOf(valor);
    }
}
