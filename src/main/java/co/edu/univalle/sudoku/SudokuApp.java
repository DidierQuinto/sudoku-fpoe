package co.edu.univalle.sudoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aplicación JavaFX del Sudoku 6x6.
 * <p>
 * Carga la vista definida en FXML (editable con Scene Builder); el FXML a su vez
 * instancia el controlador {@code SudokuController}.
 */
public class SudokuApp extends Application {

    private static final String RUTA_FXML = "/co/edu/univalle/sudoku/vista/sudoku-view.fxml";

    /**
     * Construye la ventana principal y la muestra.
     *
     * @param escenarioPrincipal ventana principal entregada por JavaFX
     * @throws IOException si no se puede cargar el archivo FXML
     */
    @Override
    public void start(Stage escenarioPrincipal) throws IOException {
        FXMLLoader cargador = new FXMLLoader(SudokuApp.class.getResource(RUTA_FXML));
        Scene escena = new Scene(cargador.load(), 900, 680);
        escenarioPrincipal.setTitle("Sudoku 6x6");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.setMinWidth(840);
        escenarioPrincipal.setMinHeight(660);
        escenarioPrincipal.show();
    }
}
