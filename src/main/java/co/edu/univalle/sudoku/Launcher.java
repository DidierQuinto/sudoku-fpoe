package co.edu.univalle.sudoku;

import javafx.application.Application;

/**
 * Punto de entrada del programa.
 * <p>
 * Existe como clase separada de {@link SudokuApp} porque, al ejecutar un proyecto JavaFX
 * sin módulos desde el IDE, la clase principal no debe extender {@code Application}
 * (de lo contrario aparece el error «JavaFX runtime components are missing»).
 * Siempre se ejecuta esta clase desde IntelliJ.
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Lanza la aplicación JavaFX.
     *
     * @param args argumentos de línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        Application.launch(SudokuApp.class, args);
    }
}
