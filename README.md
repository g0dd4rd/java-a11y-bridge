# java-a11y-bridge

A pure Java AT-SPI2 accessibility bridge for Swing and JavaFX applications on Linux.

## What it does

java-a11y-bridge replaces [java-atk-wrapper](https://gitlab.gnome.org/GNOME/java-atk-wrapper) — the existing JNI/ATK-based accessibility bridge that is barely maintained and requires native C compilation per architecture.

This bridge talks directly to AT-SPI2 over D-Bus using [dbus-java](https://github.com/hypfvieh/dbus-java), with zero native code. It makes Java applications accessible to screen readers (Orca), accessibility inspectors (Accerciser), and testing tools (Dogtail) on any Linux desktop.

## Architecture

```
javax.accessibility (Swing/JavaFX)
        |
  java-a11y-bridge
        |
    dbus-java (D-Bus protocol)
        |
  AT-SPI2 (Linux accessibility)
        |
  Orca / Accerciser / Dogtail
```

## Features

- **Zero native code** — single JAR, no JNI, no C compilation, no `--enable-native-access`
- **10 AT-SPI2 interfaces**: Accessible, Component, Action, Value, Selection, Text, Image, Table, Application, Properties
- **Event forwarding**: focus, state changes, text caret movement, window lifecycle, property changes
- **48 role mappings**, 28 state mappings, relation support
- **Works with any Swing app** — no code changes needed in the application
- Tested with SwingSet2 and custom Swing applications via Accerciser

## Requirements

- JDK 21+
- Linux with D-Bus and AT-SPI2 (any desktop: GNOME, KDE, Sway, etc.)

## Building

```bash
mvn clean package
```

## Usage

Add the bridge JAR and its dependencies to your classpath, then set the assistive technologies property:

```bash
java \
  -cp "your-app.jar:java-a11y-bridge.jar:dbus-java-core.jar:dbus-java-transport-native-unixsocket.jar:slf4j-api.jar" \
  -Djavax.accessibility.assistive_technologies=org.a11y.bridge.A11yBridge \
  your.MainClass
```

Or create an `accessibility.properties` file in `$JAVA_HOME/conf/`:

```properties
assistive_technologies=org.a11y.bridge.A11yBridge
```

## How it works

1. `A11yProvider` implements `javax.accessibility.AccessibilityProvider` (SPI)
2. On activation, it connects to the AT-SPI2 bus via `org.a11y.Bus.GetAddress()`
3. Registers the application via `org.a11y.atspi.Socket.Embed`
4. Exports each Swing `AccessibleContext` as a D-Bus object at `/org/a11y/atspi/accessible/<id>`
5. Serves accessible properties via `org.freedesktop.DBus.Properties`
6. Forwards AWT/Swing events as AT-SPI2 D-Bus signals

## Known limitations

- Tree doesn't auto-refresh on dynamic UI changes (e.g., tab switches in tabbed panes)
- TableCell interface not implemented
- Hyperlink/Hypertext not implemented
- No Cache interface for bulk tree loading (performance optimization for large UIs)
- Orca integration not yet tested (Orca crashes on the test system due to Python 3.14 incompatibility)

## License

LGPL-2.1 — same as java-atk-wrapper. Allows proprietary applications to use it as a library.
