package co.edu.univalle.sudoku.controlador;

import co.edu.univalle.sudoku.modelo.JuegoSudoku;
import co.edu.univalle.sudoku.modelo.ModeloAdapter;
import co.edu.univalle.sudoku.modelo.Posicion;
import co.edu.univalle.sudoku.modelo.ResultadoAyuda;
import co.edu.univalle.sudoku.modelo.ResultadoJugada;
import co.edu.univalle.sudoku.modelo.Tablero;
import co.edu.univalle.sudoku.vista.TableroVista;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.Set;

/**
 * Controlador del Sudoku (la «C» de Modelo-Vista-Controlador).
 * <p>
 * Recibe los eventos del usuario (botones, mouse y teclado), se los pasa al modelo
 * ({@link JuegoSudoku}) y actualiza la vista ({@link TableroVista} y las etiquetas del FXML)
 * cuando el modelo avisa de un cambio mediante un {@link ModeloAdapter}.
 */
public class SudokuController {

    /** Tipos de mensaje; cada uno corresponde a una clase CSS de {@code estilos.css}. */
    private enum TipoMensaje {
        INFO("mensaje-info"),
        EXITO("mensaje-exito"),
        ERROR("mensaje-error"),
        SUGERENCIA("mensaje-sugerencia");

        private final String claseCss;

        TipoMensaje(String claseCss) {
            this.claseCss = claseCss;
        }
    }

    @FXML
    private BorderPane raiz;
    @FXML
    private GridPane tableroGrid;
    @FXML
    private Button btnNuevoJuego;
    @FXML
    private Button btnAyuda;
    @FXML
    private Label lblVacias;
    @FXML
    private Label lblAyudas;
    @FXML
    private Label lblMensaje;
    @FXML
    private Label lblEstado;

    private final JuegoSudoku juego = new JuegoSudoku();
    private TableroVista tableroVista;
    private Posicion seleccion;

    /**
     * Lo llama JavaFX después de cargar el FXML: crea el tablero, conecta el modelo con la
     * vista y registra el manejador de teclado.
     */
    @FXML
    private void initialize() {
        tableroVista = new TableroVista(tableroGrid, this::seleccionarCelda, this::borrarCeldaConClicDerecho);

        juego.agregarListener(new ModeloAdapter() {
            @Override
            public void onJuegoIniciado(Tablero tableroInicial) {
                seleccion = null;
                tableroVista.mostrarTablero(tableroInicial);
                mostrarMensaje("¡Nuevo juego! Selecciona una celda y escribe un número del 1 al 6.", TipoMensaje.INFO);
                actualizarPanel();
            }

            @Override
            public void onCeldaCambiada(Posicion posicion, int valor) {
                tableroVista.limpiarSugerencia();
                tableroVista.mostrarValor(posicion, valor);
                actualizarPanel();
            }

            @Override
            public void onConflictosActualizados(Set<Posicion> conflictos) {
                tableroVista.marcarConflictos(conflictos);
            }

            @Override
            public void onSugerencia(Posicion posicion, int valor) {
                seleccionarCelda(posicion);
                tableroVista.mostrarSugerencia(posicion, valor);
            }

            @Override
            public void onJuegoCompletado() {
                tableroVista.marcarCompletado();
                seleccion = null;
                mostrarMensaje("¡Felicidades! Completaste el Sudoku sin errores.", TipoMensaje.EXITO);
                actualizarPanel();
                Platform.runLater(SudokuController.this::mostrarAlertaVictoria);
            }
        });

        // Filtro a nivel de toda la escena: el teclado funciona sin importar qué nodo tenga el foco.
        raiz.sceneProperty().addListener((obs, vieja, escena) -> {
            if (escena != null) {
                escena.addEventFilter(KeyEvent.KEY_PRESSED, this::alPresionarTecla);
            }
        });

        mostrarMensaje("Pulsa «Nuevo juego» para comenzar.", TipoMensaje.INFO);
        actualizarPanel();
    }

    // ------------------------------------------------------------------ botones

    /** HU-2: pide confirmación y, si el jugador acepta, inicia un juego nuevo. */
    @FXML
    private void onNuevoJuego() {
        ButtonType aceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Nuevo juego");
        alerta.setHeaderText(juego.isEnCurso()
                ? "¿Iniciar un nuevo juego?"
                : "¿Deseas iniciar un juego de Sudoku?");
        alerta.setContentText(juego.isEnCurso()
                ? "Se perderá el progreso de la partida actual."
                : "Se generará un tablero nuevo de 6x6.");
        alerta.getButtonTypes().setAll(aceptar, cancelar);
        alerta.initOwner(raiz.getScene().getWindow());

        Optional<ButtonType> respuesta = alerta.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == aceptar) {
            juego.iniciarJuego();
        }
    }

    /** HU-5: pide una sugerencia al modelo y avisa al jugador. */
    @FXML
    private void onAyuda() {
        ResultadoAyuda resultado = juego.pedirAyuda(seleccion);
        switch (resultado.estado()) {
            case OK -> mostrarMensaje("Sugerencia: prueba con el " + resultado.valor()
                    + " en la fila " + (resultado.posicion().fila() + 1)
                    + ", columna " + (resultado.posicion().columna() + 1)
                    + ". Escríbelo tú para confirmarlo.", TipoMensaje.SUGERENCIA);
            case SIN_AYUDAS -> mostrarMensaje("Ya usaste todas las ayudas de esta partida.", TipoMensaje.ERROR);
            case CON_ERRORES -> mostrarMensaje("Hay números repetidos (celdas en rojo). Corrígelos antes de pedir ayuda.",
                    TipoMensaje.ERROR);
            case SIN_OPCIONES -> mostrarMensaje("Esa celda no tiene ningún número posible: revisa lo que ya escribiste.",
                    TipoMensaje.ERROR);
            case SIN_CELDAS_VACIAS -> mostrarMensaje("No quedan celdas vacías.", TipoMensaje.INFO);
            case SIN_JUEGO -> mostrarMensaje("Primero inicia un juego con «Nuevo juego».", TipoMensaje.INFO);
        }
        actualizarPanel();
    }

    // ------------------------------------------------------------------ mouse

    /** HU-3: clic izquierdo, selecciona una celda. */
    private void seleccionarCelda(Posicion posicion) {
        if (!juego.isEnCurso()) {
            mostrarMensaje("Pulsa «Nuevo juego» para comenzar.", TipoMensaje.INFO);
            return;
        }
        seleccion = posicion;
        tableroVista.seleccionar(posicion);
    }

    /** Clic derecho: selecciona la celda y borra su número. */
    private void borrarCeldaConClicDerecho(Posicion posicion) {
        seleccionarCelda(posicion);
        borrarSeleccion();
    }

    // ------------------------------------------------------------------ teclado

    /** HU-3: teclas 1-6 escriben, Supr/Retroceso borran y las flechas mueven la selección. */
    private void alPresionarTecla(KeyEvent evento) {
        KeyCode codigo = evento.getCode();
        switch (codigo) {
            case UP -> moverSeleccion(-1, 0);
            case DOWN -> moverSeleccion(1, 0);
            case LEFT -> moverSeleccion(0, -1);
            case RIGHT -> moverSeleccion(0, 1);
            case BACK_SPACE, DELETE -> borrarSeleccion();
            default -> {
                String texto = evento.getText();
                if (texto != null && texto.length() == 1 && Character.isDigit(texto.charAt(0))) {
                    ingresarDigito(Character.digit(texto.charAt(0), 10));
                } else {
                    return; // tecla que no usamos: se deja pasar
                }
            }
        }
        evento.consume();
    }

    private void moverSeleccion(int deltaFila, int deltaColumna) {
        if (!juego.isEnCurso()) {
            return;
        }
        int fila = seleccion == null ? 0 : seleccion.fila() + deltaFila;
        int columna = seleccion == null ? 0 : seleccion.columna() + deltaColumna;
        fila = Math.max(0, Math.min(Tablero.TAMANO - 1, fila));
        columna = Math.max(0, Math.min(Tablero.TAMANO - 1, columna));
        seleccionarCelda(new Posicion(fila, columna));
    }

    // ------------------------------------------------------------------ jugadas

    /** HU-3 y HU-4: escribe un número en la celda seleccionada y muestra el resultado de la validación. */
    private void ingresarDigito(int digito) {
        if (!hayJuegoYSeleccion()) {
            return;
        }
        if (digito < 1 || digito > Tablero.TAMANO) {
            mostrarMensaje("Solo se permiten números del 1 al 6.", TipoMensaje.ERROR);
            return;
        }
        ResultadoJugada resultado = juego.ingresarNumero(seleccion, digito);
        switch (resultado) {
            case OK -> {
                if (juego.isEnCurso()) {
                    mostrarMensaje("Escribiste el " + digito + ".", TipoMensaje.INFO);
                }
            }
            case CON_CONFLICTO -> mostrarMensaje(juego.describirConflicto(seleccion), TipoMensaje.ERROR);
            case CELDA_FIJA -> mostrarMensaje("Esa celda es parte del tablero inicial y no se puede cambiar.",
                    TipoMensaje.ERROR);
            default -> {
            }
        }
    }

    /** HU-3: borra el número de la celda seleccionada. */
    private void borrarSeleccion() {
        if (!hayJuegoYSeleccion()) {
            return;
        }
        ResultadoJugada resultado = juego.borrarNumero(seleccion);
        switch (resultado) {
            case OK -> mostrarMensaje("Número eliminado.", TipoMensaje.INFO);
            case YA_VACIA -> mostrarMensaje("La celda ya está vacía.", TipoMensaje.INFO);
            case CELDA_FIJA -> mostrarMensaje("Esa celda es parte del tablero inicial y no se puede borrar.",
                    TipoMensaje.ERROR);
            default -> {
            }
        }
    }

    private boolean hayJuegoYSeleccion() {
        if (!juego.isEnCurso()) {
            mostrarMensaje("Pulsa «Nuevo juego» para comenzar.", TipoMensaje.INFO);
            return false;
        }
        if (seleccion == null) {
            mostrarMensaje("Primero selecciona una celda con el mouse o las flechas.", TipoMensaje.INFO);
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ vista auxiliar

    private void mostrarMensaje(String texto, TipoMensaje tipo) {
        for (TipoMensaje t : TipoMensaje.values()) {
            lblMensaje.getStyleClass().remove(t.claseCss);
        }
        lblMensaje.getStyleClass().add(tipo.claseCss);
        lblMensaje.setText(texto);
    }

    private void actualizarPanel() {
        boolean hayPartida = juego.isEnCurso();
        lblVacias.setText("Celdas vacías: " + (hayPartida ? juego.getCeldasVacias() : "–"));
        lblAyudas.setText("Ayudas disponibles: " + (hayPartida ? juego.getAyudasRestantes() : "–"));
        btnAyuda.setText(hayPartida ? "Ayuda (" + juego.getAyudasRestantes() + ")" : "Ayuda");
        btnAyuda.setDisable(!hayPartida || juego.getAyudasRestantes() == 0);
        lblEstado.setText(hayPartida ? "Juego en curso" : "Sin juego en curso");
    }

    private void mostrarAlertaVictoria() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Sudoku completado");
        alerta.setHeaderText("¡Felicidades!");
        alerta.setContentText("Completaste el tablero sin errores. Puedes empezar otra partida con «Nuevo juego».");
        alerta.initOwner(raiz.getScene().getWindow());
        alerta.showAndWait();
    }
}
