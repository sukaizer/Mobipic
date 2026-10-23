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

* Load a base image (jpg, png, bmp, gif) to start a project, drop it on the window, or paste it (Ctrl+V)
* Add other images

### Drawing, like in Paint

* **Tool palette** on the left: selection, brush, rectangle, rounded rectangle, ellipse, triangle, line, text
  and eyedropper. A tool stays active, so several shapes can be drawn in a row.
* **Two colours**, as in Paint: the primary colour is used for outlines, brush strokes and text, the secondary
  one for fills and text outlines. Pick them from the palette (left click: primary, right click: secondary),
  with the colour pickers, or from the image with the eyedropper. `X` swaps them.
* **Options bar** under the toolbar: outline and fill colours, outline only / fill only / both, width, corner
  radius, opacity; for text: font, size, bold, italic, alignment, outline and shadow. With the selection tool,
  it shows and edits the style of the selected layer.
* **Shapes** are drawn by dragging, in any direction. `Shift` draws squares and circles and snaps lines to 45°,
  `Alt` draws from the centre. Triangles can also be placed with three clicks.
* **Brush** for freehand drawing; each stroke is a layer that can be moved, resized and rotated.
* **Text** is typed directly on the image: click where it should go, type (several lines allowed), then press
  `Esc` or click elsewhere. Double-click a text (or click it with the text tool) to edit it.

### Editing layers

* Click a layer on the canvas or in the list to select it, then:
  * drag it to move it,
  * drag one of its handles to resize it (`Shift` keeps the proportions; resizing a text scales its font),
  * drag the round handle above it to rotate it (`Shift` snaps to 15°), or rotate by 90° from the Layer menu
* Duplicate, delete, cut, copy and paste layers
* Layer panel: thumbnails, show / hide each layer, rename it (double-click or `F2`), reorder by drag and drop
  or with the arrows (the base image always stays at the bottom)
* Opacity per layer
* Add a colour filter to an image, with a live preview
* Undo / redo (50 steps)

### Viewing and saving

* Zoom in and out, with scroll bars and Ctrl + mouse wheel
* Export the image to jpg, png or bmp, always at 100% scale (hidden layers are not exported)
* Save your project as a single, portable `.mbpc` file and open it later to continue editing
* Prompt before losing unsaved changes

### Keyboard shortcuts

`Ctrl` is `Cmd` on macOS. Single-letter shortcuts are ignored while typing text.

| Shortcut                          | Action                                         |
|-----------------------------------|------------------------------------------------|
| `V` `B` `R` `U` `E` `G` `L` `T` `I` | Select, brush, rectangle, rounded rectangle, ellipse, triangle, line, text, eyedropper |
| `X`                               | Swap primary and secondary colours             |
| `Ctrl+N`                          | Load an image as a new project                 |
| `Ctrl+O`                          | Open an existing project                       |
| `Ctrl+S` / `Ctrl+Shift+S`         | Save / save as                                 |
| `Ctrl+Q`                          | Quit                                           |
| `Ctrl+Z` / `Ctrl+Y`               | Undo / redo                                    |
| `Ctrl+X` / `Ctrl+C` / `Ctrl+V`    | Cut / copy / paste a layer (or paste an image) |
| `Del`, `Backspace`, `Ctrl+Backspace` | Delete the selected layer                   |
| `Ctrl+D`                          | Duplicate the selected layer                   |
| `F2`                              | Rename the selected layer                      |
| `Ctrl+Shift+L` / `Ctrl+Shift+R`   | Rotate 90° left / right                        |
| Arrow keys (`+Shift`)             | Nudge the selected layer 1 px (10 px)          |
| `Ctrl++` / `Ctrl+-` / `Ctrl+0`    | Zoom in / out / actual size                    |
| `Esc`                             | Finish typing, cancel the current drawing, clear the selection |
| `Ctrl+B` / `Ctrl+R`               | Kept from version 1: switch to the selection tool |

### Icon bar

The toolbar buttons, in order: open a project, open an image as a new project, save the project,
shape tool, add an image, text tool, zoom in, zoom out, actual size.

## Project file format

A `.mbpc` file is a zip archive containing:

* `project.json`: format version, canvas size and the layers (bottom first; the first one is the base image),
  each with its name, visibility, opacity and rotation
* `images/<n>.png`: the images used by image layers, stored once even when a layer is duplicated

Mobipic 2.1 writes format 2 and still opens the format 1 projects saved by Mobipic 2.0.
Projects from Mobipic 1.x (folder with a text file and absolute image paths) are not supported.

## Code structure

```
src/main/java/com/mobipic/
  app/         application entry point and splash screen
  model/       layers (sealed hierarchy), styles, project model, undo history - no UI dependency
  render/      canvas painting (layers, selection outline, handles)
  tool/        canvas tools (select/transform, shapes, brush, text, eyedropper) and shared tool settings
  io/          project files (.mbpc) and image export
  controller/  JavaFX controllers for the FXML views
  util/        i18n, FXML loading, dialogs
src/main/resources/com/mobipic/
  fxml/  css/  images/  i18n/
```

## History

* **2.1** (2026): Paint-like drawing: tool palette, options bar, primary / secondary colours and palette,
  eyedropper, brush, rounded rectangle, outline + fill, Shift / Alt modifiers, text typed on the canvas
  (multi-line, outline, shadow), direct move / resize / rotate, opacity, layer thumbnails, visibility,
  renaming, drag-and-drop reordering, clipboard. Project format 2.
* **2.0** (2026): Java 21 + JavaFX 21, Maven build, modular application, rewritten model and
  tools, undo/redo, portable project format, i18n (French / English), bug fixes, tests.
* **1.0** (2021): university project, Java/JavaFX 15 then 16, built with IntelliJ and SceneBuilder.
