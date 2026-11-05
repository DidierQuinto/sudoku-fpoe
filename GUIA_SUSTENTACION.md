# Guía para la sustentación

Según el enunciado, la sustentación (15 min, en los computadores de la sala) consiste en **hacer un cambio
al proyecto** que se asigna ese día y **responder preguntas sobre el código**. Si no se puede explicar el
código, la nota es 0. Esta guía es para que cada integrante conozca el proyecto a fondo.

## 1. Qué pasa cuando el jugador presiona la tecla «4»

1. `SudokuController.alPresionarTecla` (filtro de teclado en la escena) recibe el `KeyEvent`.
2. Lee el carácter y llama a `ingresarDigito(4)`.
3. `ingresarDigito` llama a `juego.ingresarNumero(seleccion, 4)` (**modelo**).
4. `JuegoSudoku` comprueba que hay juego, que el valor es 1-6 y que la celda no es fija; escribe el número
   en `Tablero.setValor` y llama a `despuesDeCambio`.
5. `despuesDeCambio` recalcula los conflictos (`getConflictos`) y **notifica a los oyentes**:
   `onCeldaCambiada`, `onConflictosActualizados` y, si el tablero está lleno y sin errores, `onJuegoCompletado`.
6. El `ModeloAdapter` anónimo creado en `SudokuController.initialize` recibe esos eventos y le pide a
   `TableroVista` que pinte el número y los bordes rojos.
7. El método devuelve un `ResultadoJugada` y el controlador muestra el mensaje (por ejemplo,
   «El 4 ya está en la fila 2 y en el bloque 3.»).

Idea clave: **el modelo no conoce la vista**; solo avisa por la interfaz `ModeloListener`.

## 2. Preguntas que conviene saber responder

* **¿Por qué hay un `Launcher`?** Si la clase principal extiende `Application` y el proyecto no usa módulos,
  JavaFX falla al ejecutarse desde el IDE. `Launcher` no extiende `Application` y la lanza con
  `Application.launch`.
* **¿Dónde está la interfaz y dónde el adaptador?** `ModeloListener` (interfaz con 5 eventos) y
  `ModeloAdapter` (implementa todo vacío). `SudokuController` extiende el adaptador con una clase anónima.
* **¿Cómo se genera el tablero?** `GeneradorTablero.generarSolucion` llena el 6×6 con *backtracking*
  probando candidatos en orden aleatorio; luego `generarTableroInicial` deja 2 números por bloque tomados
  de esa solución. Por eso siempre es válido y tiene solución.
* **¿Cómo se valida?** `Tablero.puedeColocar` revisa fila, columna y bloque 2×3 ignorando la propia celda;
  `calcularConflictos` lo aplica a toda celda con número. El bloque de una celda se calcula con
  `fila / 2` y `columna / 3` (`Tablero.indiceBloque`).
* **¿Cómo asegura la ayuda que no se resuelva sola?** `pedirAyuda` **no escribe** en el tablero (solo
  genera una sugerencia que la vista muestra en amarillo), el jugador debe escribir el número, y
  además hay un máximo de `MAX_AYUDAS = 3`.
* **¿Qué estructuras de datos usa?** Matrices `int[6][6]` / `boolean[6][6]`, `List<Integer>` (candidatos),
  `Set<Posicion>` (conflictos), `List<ModeloListener>` (oyentes).
* **¿Qué layouts usa la interfaz?** `BorderPane` (estructura), `VBox`/`HBox` (paneles), `StackPane`
  (centra el tablero), `GridPane` (el tablero: uno exterior de 3×2 bloques y uno interior de 2×3 celdas por bloque).
* **¿Qué eventos de mouse y teclado hay?** Clic izquierdo (seleccionar) y derecho (borrar) en
  `TableroVista.crearCelda`; teclas 1-6, Supr/Retroceso y flechas en `SudokuController.alPresionarTecla`.

## 3. Cambios típicos y dónde se hacen

| Cambio pedido | Dónde tocar |
|---|---|
| Cambiar el máximo de ayudas | `JuegoSudoku.MAX_AYUDAS` |
| Cambiar cuántos números aparecen por bloque al inicio | `GeneradorTablero.NUMEROS_POR_BLOQUE` |
| Cambiar colores, bordes o fuentes | `estilos.css` |
| Cambiar el tamaño de las celdas | `TableroVista.TAMANO_CELDA` |
| Agregar un botón (p. ej. «Borrar todo» o «Reiniciar») | `sudoku-view.fxml` (botón con `onAction`) + método `@FXML` en `SudokuController` + método nuevo en `JuegoSudoku` |
| Mostrar el número de errores en el panel | `SudokuController.actualizarPanel` (usar `juego.getConflictos().size()`) y una etiqueta nueva en el FXML |
| Cambiar teclas o agregar un atajo | `SudokuController.alPresionarTecla` |
| Que la ayuda muestre todos los candidatos de la celda | `Tablero.candidatos` + `JuegoSudoku.pedirAyuda` + `TableroVista.mostrarSugerencia` |
| Cambiar los mensajes de error | `Tablero.describirConflicto` y los `mostrarMensaje` del controlador |

## 4. Cómo estudiar el código

1. Ejecuten el juego y prueben cada historia de usuario (HU-1 a HU-5).
2. Pongan un *breakpoint* en `JuegoSudoku.ingresarNumero` y sigan una jugada con el depurador de IntelliJ (F7/F8).
3. **Hagan ustedes mismos 3 o 4 cambios de la tabla anterior sin mirar esta guía**, cronometrándose
   (el límite en la sustentación es de 15 minutos).
4. Cada integrante debe poder explicar *todas* las clases, no solo las de su rama.
5. Practiquen en un computador distinto al suyo: abrir el repositorio desde GitHub, ejecutar y modificar.
