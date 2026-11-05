# Guía de trabajo en equipo con Git y GitHub (3 integrantes)

El enunciado pide **al menos 2 Pull Requests y 2 commits por estudiante**, y el profesor lo revisa en el
historial de GitHub. Por eso cada integrante debe hacer **sus propios commits y sus propios PR desde su
cuenta**. Esta guía reparte el proyecto para que eso salga de forma natural.

## 1. Reparto sugerido (por capas MVC)

| Integrante | Rama | Archivos (carpeta `src/main/...`) |
|---|---|---|
| **A — Modelo** | `feature/modelo` | `java/.../modelo/Posicion.java`, `Tablero.java`, `GeneradorTablero.java`, `ResultadoJugada.java`, `ResultadoAyuda.java` |
| **B — Vista** | `feature/vista` | `resources/.../vista/sudoku-view.fxml`, `resources/.../vista/estilos.css`, `java/.../vista/TableroVista.java` |
| **C — Controlador y partida** | `feature/controlador` | `java/.../modelo/JuegoSudoku.java`, `ModeloListener.java`, `ModeloAdapter.java`, `java/.../controlador/SudokuController.java`, `SudokuApp.java`, `Launcher.java` |

Orden de integración (por las dependencias entre clases): **A → B → C**.
Mientras A no haya hecho merge, B y C no compilan del todo; es normal. Cuando A haga merge, B y C hacen
`git pull origin main` en sus ramas.

## 2. Preparación (una sola vez)

1. **Quien sea líder** crea el repositorio en GitHub (privado, sin README) y en
   *Settings → Collaborators* invita a los otros dos.
2. El líder sube a `main` un primer commit con los archivos base:
   `pom.xml`, `.gitignore`, `README.md`, `GUIA_*.md`.
   ```bash
   git init
   git add pom.xml .gitignore README.md GUIA_EQUIPO_GIT.md GUIA_SUSTENTACION.md
   git commit -m "chore: estructura base del proyecto Maven con JavaFX"
   git branch -M main
   git remote add origin https://github.com/<usuario>/<repositorio>.git
   git push -u origin main
   ```
3. Los otros dos clonan en IntelliJ: **File → New → Project from Version Control** → pegan la URL.
4. Cada uno verifica que Git sabe quién es (así los commits salen con su nombre):
   ```bash
   git config user.name  "Nombre Apellido"
   git config user.email "correo-de-su-cuenta-de-github"
   ```

## 3. Flujo de cada integrante (PR #1: su parte principal)

```bash
git checkout -b feature/modelo           # (o feature/vista, feature/controlador)
# copia a tu carpeta los archivos que te tocan (ver tabla) y luego:
git add src/main/java/co/edu/univalle/sudoku/modelo/Posicion.java src/main/java/co/edu/univalle/sudoku/modelo/Tablero.java
git commit -m "feat(modelo): agrega Posicion y Tablero con reglas del Sudoku"
git add src/main/java/co/edu/univalle/sudoku/modelo/GeneradorTablero.java ...
git commit -m "feat(modelo): agrega generador de tablero con 2 números por bloque"
git push -u origin feature/modelo
```

* Haz **mínimo 2 commits** separados (por ejemplo, uno por grupo de archivos) con mensajes claros.
* En GitHub aparece el botón **Compare & pull request** (o en IntelliJ: *Git → GitHub → Create Pull Request*).
  Título y descripción cortos explicando qué incluye.
* **Otro compañero revisa** (pestaña *Files changed*, deja al menos un comentario) y hace **Merge**.

## 4. PR #2 (una mejora pequeña por persona)

Cada uno crea otra rama desde `main` actualizado (`git checkout main && git pull`) y hace un segundo PR
con un cambio real. Ideas:

* **A:** agregar pruebas unitarias del `Tablero` (JUnit 5) o revisar/mejorar el Javadoc del modelo.
* **B:** ajustar colores/tamaños en `estilos.css`, y agregar una captura de pantalla al `README.md`.
* **C:** mejorar los mensajes del controlador o agregar un contador de errores en el panel lateral.

Cada PR #2 también debe tener su(s) commit(s). Así cada integrante termina con **2 PR y ≥ 2 commits**.

## 5. Buenas prácticas y problemas típicos

* No trabajen todos sobre `main`; siempre ramas + PR.
* Antes de empezar a trabajar: `git pull origin main`. Antes de subir: `git status` y revisar qué se agrega.
* **Conflicto al hacer merge:** IntelliJ muestra *Merge Conflicts*; elijan qué líneas se quedan, marquen como
  resuelto, `git add` y `git commit`.
* Si subieron la carpeta `.idea/` o `target/` por error: ya están en `.gitignore`; quítenlas con
  `git rm -r --cached .idea target` y hagan commit.
* Todos deben poder abrir el proyecto, ejecutarlo y **explicar el código completo**, no solo su parte:
  en la sustentación el profesor pregunta por cualquier clase (ver `GUIA_SUSTENTACION.md`).
