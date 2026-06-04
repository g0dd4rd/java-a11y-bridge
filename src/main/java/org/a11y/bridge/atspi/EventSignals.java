package org.a11y.bridge.atspi;

import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

public final class EventSignals {

    private EventSignals() {}

    @DBusInterfaceName("org.a11y.atspi.Event.Object")
    public interface ObjectEvents extends DBusInterface {
        class PropertyChange extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public PropertyChange(String path, String minor, int detail1, int detail2,
                                  Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class StateChanged extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public StateChanged(String path, String minor, int detail1, int detail2,
                                Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class ChildrenChanged extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public ChildrenChanged(String path, String minor, int detail1, int detail2,
                                   Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class TextCaretMoved extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public TextCaretMoved(String path, String minor, int detail1, int detail2,
                                  Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class TextChanged extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public TextChanged(String path, String minor, int detail1, int detail2,
                               Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class SelectionChanged extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public SelectionChanged(String path, String minor, int detail1, int detail2,
                                    Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }
    }

    @DBusInterfaceName("org.a11y.atspi.Event.Focus")
    public interface FocusEvents extends DBusInterface {
        class Focus extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public Focus(String path, String minor, int detail1, int detail2,
                         Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }
    }

    @DBusInterfaceName("org.a11y.atspi.Event.Window")
    public interface WindowEvents extends DBusInterface {
        class Activate extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public Activate(String path, String minor, int detail1, int detail2,
                            Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class Deactivate extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public Deactivate(String path, String minor, int detail1, int detail2,
                              Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class Create extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public Create(String path, String minor, int detail1, int detail2,
                          Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }

        class Destroy extends DBusSignal {
            public final String minor;
            public final int detail1;
            public final int detail2;
            public final Variant<?> anyData;
            public final Map<String, Variant<?>> properties;

            public Destroy(String path, String minor, int detail1, int detail2,
                           Variant<?> anyData, Map<String, Variant<?>> properties)
                    throws DBusException {
                super(path, minor, detail1, detail2, anyData, properties);
                this.minor = minor;
                this.detail1 = detail1;
                this.detail2 = detail2;
                this.anyData = anyData;
                this.properties = properties;
            }
        }
    }
}
