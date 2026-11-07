# Sudoku 6×6 / Miniproyecto #2

Juego de Sudoku de 6×6 hecho en **Java + JavaFX** con arquitectura **Modelo-Vista-Controlador**.
Cada fila, columna y bloque de **2 filas × 3 columnas** debe contener los números del 1 al 6 sin repetir.

## Cómo ejecutarlo en IntelliJ IDEA

1. **File → Open** y elige la carpeta del proyecto (la que contiene `pom.xml`). Acepta *Trust Project*.
2. Espera a que Maven descargue las dependencias (JavaFX 21). Necesitas **JDK 17 o superior**
   (*File → Project Structure → Project → SDK*).
3. Abre `src/main/java/co/edu/univalle/sudoku/Launcher.java` y pulsa el triángulo verde junto a `main`.
   > Ejecuta **`Launcher`**, no `SudokuApp`: si ejecutas la clase que extiende `Application` desde el IDE
   > aparece el error «JavaFX runtime components are missing».

Alternativa por terminal: `mvn javafx:run`.

Para editar la interfaz con **Scene Builder**: clic derecho sobre
`src/main/resources/co/edu/univalle/sudoku/vista/sudoku-view.fxml` → *Open in SceneBuilder*.

Para generar el **Javadoc**: *Tools → Generate JavaDoc* en IntelliJ, o `mvn javadoc:javadoc`
(queda en `target/site/apidocs/index.html`).

## Cómo se juega

| Acción | Cómo |
|---|---|
| Empezar | Botón **Nuevo juego** y aceptar la alerta de confirmación |
| Seleccionar celda | Clic izquierdo, o flechas del teclado |
| Escribir número | Teclas **1 a 6** (otras teclas numéricas muestran un aviso) |
| Borrar número | **Supr** / **Retroceso**, o clic derecho sobre la celda |
| Ayuda | Botón **Ayuda**: sugiere un número válido (máx. 3 por partida), pero **no lo escribe** |

Las celdas con borde **rojo** tienen un número repetido; abajo a la derecha se explica dónde se repite.

## Estructura (MVC)

```
src/main/java/co/edu/univalle/sudoku/
├── Launcher.java              Punto de entrada
├── SudokuApp.java             Application: carga el FXML
├── modelo/                    M — reglas del juego, sin nada de JavaFX
│   ├── Posicion.java          record (fila, columna)
│   ├── Tablero.java           matriz 6x6, reglas, conflictos, candidatos
│   ├── GeneradorTablero.java  genera solución (backtracking) y tablero inicial (2 por bloque)
│   ├── JuegoSudoku.java       partida: iniciar, ingresar, borrar, ayuda, avisos
│   ├── ModeloListener.java    INTERFAZ de eventos del modelo
│   ├── ModeloAdapter.java     CLASE ADAPTADORA (métodos vacíos)
│   ├── ResultadoJugada.java   enum con el resultado de una jugada
│   └── ResultadoAyuda.java    record con el resultado de pedir ayuda
├── vista/
│   └── TableroVista.java      V — dibuja las 36 celdas y aplica estilos
└── controlador/
    └── SudokuController.java  C — eventos de botones, mouse y teclado

src/main/resources/co/edu/univalle/sudoku/vista/
├── sudoku-view.fxml           V — interfaz (Scene Builder)
└── estilos.css                V — colores y estilos
```

## Dónde se cumple cada requisito del enunciado

| Requisito | Dónde |
|---|---|
| Interfaz intuitiva y atractiva (HU-1) | `sudoku-view.fxml`, `estilos.css`, `TableroVista` |
| Interfaz o clase adaptadora | `ModeloListener` (interfaz) y `ModeloAdapter` (adaptador, extendido en `SudokuController`) |
| Eventos de mouse y teclado | `TableroVista.crearCelda` (clics) y `SudokuController.alPresionarTecla` (teclado) |
| Modelo-Vista-Controlador | Paquetes `modelo`, `vista`, `controlador` |
| Javadoc | Todas las clases y métodos públicos |
| Layouts | `BorderPane`, `VBox`, `HBox`, `StackPane`, `GridPane` (el tablero usa `GridPane` anidados: uno por bloque 2×3) |
| Estructura de datos | Matrices `int[6][6]` y `boolean[6][6]`, `List`, `Set` (en `Tablero`, `JuegoSudoku`) |
| Inicio del juego con confirmación (HU-2) | `SudokuController.onNuevoJuego`, `JuegoSudoku.iniciarJuego`, `GeneradorTablero` |
| Ingreso de números 1-6 y borrado (HU-3) | `SudokuController.ingresarDigito` / `borrarSeleccion`, `JuegoSudoku.ingresarNumero` |
| Validación en tiempo real con borde rojo y mensaje (HU-4) | `Tablero.calcularConflictos` / `describirConflicto`, `.celda-conflicto` en el CSS |
| Ayuda limitada que no resuelve el tablero (HU-5) | `JuegoSudoku.pedirAyuda`: solo sugiere, máx. 3 |



