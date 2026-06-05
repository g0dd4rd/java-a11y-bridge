# java-a11y-bridge

Pure Java AT-SPI2 accessibility for Swing and JavaFX applications on Linux. Zero native code.

## What it does

Replaces [java-atk-wrapper](https://gitlab.gnome.org/GNOME/java-atk-wrapper) — the existing JNI/ATK-based accessibility bridge that is barely maintained and requires native C compilation per architecture.

This project talks directly to AT-SPI2 over D-Bus using [dbus-java](https://github.com/hypfvieh/dbus-java). It makes Java applications accessible to screen readers (Orca), accessibility inspectors (Accerciser), and testing tools (Dogtail) on any Linux desktop.

There are two modules:

- **java-a11y-bridge** — the core bridge that translates `javax.accessibility` to AT-SPI2 D-Bus protocol. Works with Swing apps out of the box.
- **javafx-a11y-adapter** — translates JavaFX's `AccessibleAttribute`/`AccessibleAction` API to `javax.accessibility`, feeding it into the bridge. Activated via `-javaagent` with no app code changes. This exists because JavaFX on Linux has [no built-in accessibility](https://wiki.openjdk.org/display/wakefield) — `GtkApplication.createAccessible()` returns null.

## Architecture

```
Swing app                          JavaFX app
    |                                  |
javax.accessibility              AccessibleAttribute
    |                                  |
    |                          javafx-a11y-adapter
    |                          (javaagent, no code changes)
    |                                  |
    +----------------------------------+
    |
java-a11y-bridge
(javax.accessibility -> D-Bus)
    |
dbus-java
    |
AT-SPI2 D-Bus protocol
    |
Orca / Accerciser / Dogtail
```

## Requirements

- JDK 21+
- Linux with D-Bus and AT-SPI2 (any desktop — GNOME, KDE, Sway, Hyprland, etc.)
- dbus-java 5.2.0 (pulled automatically by Maven)

## Building

```bash
# Build the bridge and install to local Maven repo
mvn clean install

# Build the JavaFX adapter
cd javafx-a11y-adapter
mvn clean package
```

## Usage

### Swing applications

Add the bridge JAR and its dependencies to your classpath, then set the assistive technologies property:

```bash
java \
  -cp "your-app.jar:java-a11y-bridge.jar:dbus-java-core.jar:dbus-java-transport-native-unixsocket.jar:slf4j-api.jar" \
  -Djavax.accessibility.assistive_technologies=org.a11y.bridge.A11yBridge \
  your.MainClass
```

Or set it system-wide in `$JAVA_HOME/conf/accessibility.properties`:

```properties
assistive_technologies=org.a11y.bridge.A11yBridge
```

No application code changes are needed.

### JavaFX applications

Add both JARs to the classpath and use `-javaagent`:

```bash
java \
  --module-path /path/to/javafx/lib --add-modules javafx.controls \
  -javaagent:javafx-a11y-adapter.jar \
  -cp "your-app.jar:javafx-a11y-adapter.jar:java-a11y-bridge.jar:dbus-java-core.jar:dbus-java-transport-native-unixsocket.jar:slf4j-api.jar" \
  your.MainClass
```

No application code changes are needed. The agent automatically detects JavaFX windows, walks the scene graph, and registers all nodes with the AT-SPI2 bridge.

## What's implemented

### AT-SPI2 interfaces (bridge)

| Interface | Status |
|---|---|
| Accessible | Properties, tree traversal, roles, states, relations |
| Component | Position, size, layer, focus |
| Action | Click, toggle, activate |
| Text | Read content, caret tracking, selection |
| Value | Current/min/max values |
| Selection | Select/deselect children |
| Table | Row/column count, cell access, headers, selection |
| Image | Description, locale |
| Application | Toolkit name, version, PID |
| Properties | D-Bus property access for all interfaces |

### Event forwarding

- Focus and state changes (focused, checked, selected, enabled, expanded)
- Text caret movement
- Window activate/deactivate
- Property changes (name, description, value)
- Children added/removed

### JavaFX adapter

- 49 JavaFX `AccessibleRole` → `javax.accessibility.AccessibleRole` mappings
- Full scene graph traversal with dynamic child tracking
- Automatic window detection via `Window.getWindows()` listener
- AccessibleComponent, AccessibleAction, AccessibleText, AccessibleValue translation
- Focus/visibility/disabled state forwarding via JavaFX property listeners

## Tested with

- **RetroApp** — custom Swing app (44 nodes: buttons, text fields, menus, radio buttons, checkboxes, scroll panes)
- **SwingSet2** — JDK's Swing demo with every widget type
- **HelloWorldFX** — minimal JavaFX app (4 nodes, zero code changes)
- **FxTestApp** — complex JavaFX app (446 nodes: tabs, tables, trees, forms, sliders, spinners, lists)

All verified via Accerciser and AT-SPI2 queries.

## Known limitations

- Dynamic UI changes (e.g., tab switches) don't always trigger tree refresh in Accerciser
- TableCell interface not implemented
- Hyperlink/Hypertext not implemented
- No Cache interface for bulk tree loading
- Orca not tested (crashes on test system due to Python 3.14 incompatibility, unrelated to bridge)
- JavaFX adapter requires `-javaagent` flag (cannot use `assistive_technologies` property because JavaFX doesn't initialize AWT toolkit)

## License

LGPL-2.1 — same as java-atk-wrapper. Allows proprietary applications to use it as a library.
