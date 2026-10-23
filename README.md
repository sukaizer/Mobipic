# Mobipic

Welcome to **Mobipic**, a third year project at Paris-Saclay University.
It is a basic photo editor written in Java using JavaFX.

Version 2 modernizes the original 2021 project (Java 16, IntelliJ-only build) while keeping
the same features, look and spirit.

### Contributors
* @sukaizer
* @paulcatillon

## Getting started

Requirements: **JDK 21 or newer**. Nothing else: Maven and JavaFX are downloaded by the wrapper.

```bash
./mvnw javafx:run          # run the application (mvnw.cmd on Windows)
./mvnw verify              # compile and run the tests
./mvnw javafx:jlink        # build a self-contained runtime image in target/mobipic
```

The runtime image does not need Java installed: start it with `target/mobipic/bin/mobipic`.
A project or image path can be passed on the command line to open it at startup.

The interface is in French by default and in English on English systems
(force it with `-Duser.language=en` or `-Duser.language=fr`).

## Features

* Load a base image (jpg, png, bmp, gif) to start a project, or drop it on the window
* Add other images
* Draw shapes, in any drag direction:
  * Square
  * Rectangle
  * Triangle (three clicks, with a live preview)
  * Circle
  * Ellipse
  * Basic straight line
* Draw text: any installed font, size, colour, *italic* and/or **bold**
* Select a layer from the list or by clicking it on the canvas
* Move, resize (corner / end-point handles; text cannot be resized), duplicate and delete any layer
* Add a colour filter to an image, with a live preview
* Send a layer to the front or to the back (the base layer is not concerned)
* Undo / redo (50 steps)
* Zoom in and out, with scroll bars and Ctrl + mouse wheel
* Export the image to jpg, png or bmp, always at 100% scale
* Save your project as a single, portable `.mbpc` file and open it later to continue editing
* Prompt before losing unsaved changes

### Keyboard shortcuts

`Ctrl` is `Cmd` on macOS.

| Shortcut                 | Action                                  |
|--------------------------|-----------------------------------------|
| `Ctrl+N`                 | Load an image as a new project          |
| `Ctrl+O`                 | Open an existing project                |
| `Ctrl+S` / `Ctrl+Shift+S`| Save / save as                          |
| `Ctrl+Q`                 | Quit                                    |
| `Ctrl+Z` / `Ctrl+Y`      | Undo / redo                             |
| `Ctrl+Backspace`, `Del`  | Delete the selected layer               |
| `Ctrl+B`                 | Move mode                               |
| `Ctrl+R`                 | Resize mode                             |
| `Ctrl+D`                 | Duplicate the selected layer            |
| Arrow keys (`+Shift`)    | Nudge the selected layer 1 px (10 px)   |
| `Ctrl++` / `Ctrl+-`      | Zoom in / out                           |
| `Ctrl+0`                 | Actual size (100%)                      |
| `Esc`                    | Leave the current mode, clear selection |

### Icon bar

The toolbar buttons, in order: open a project, open an image as a new project, save the project,
draw a shape (opens the shape selector), add an image, add text (opens the text editor),
zoom in, zoom out, actual size.

## Project file format

A `.mbpc` file is a zip archive containing:

* `project.json`: format version, canvas size and the layers (bottom first; the first one is the base image)
* `images/<n>.png`: the images used by image layers, stored once even when a layer is duplicated

Projects from Mobipic 1.x (folder with a text file and absolute image paths) are not supported.

## Code structure

```
src/main/java/com/mobipic/
  app/         application entry point and splash screen
  model/       layers (sealed hierarchy), project model, undo history - no UI dependency
  render/      canvas painting (layers, selection outline, handles)
  tool/        canvas interaction modes: select, move, resize, draw shape
  io/          project files (.mbpc) and image export
  controller/  JavaFX controllers for the FXML views
  util/        i18n, FXML loading, dialogs
src/main/resources/com/mobipic/
  fxml/  css/  images/  i18n/
```

## History

* **2.0** (2026): Java 21 + JavaFX 21, Maven build, modular application, rewritten model and
  tools, undo/redo, portable project format, i18n (French / English), bug fixes, tests.
* **1.0** (2021): university project, Java/JavaFX 15 then 16, built with IntelliJ and SceneBuilder.
