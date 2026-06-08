package org.a11y.bridge.atspi;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.a11y.atspi.Socket")
public interface SocketIface extends DBusInterface {

    PlugRef Embed(PlugRef plug);

    void Unembed(PlugRef plug);

    class PlugRef extends Struct {
        @Position(0)
        private final String busName;
        @Position(1)
        private final DBusPath objectPath;

        public PlugRef(String busName, DBusPath objectPath) {
            this.busName = busName;
            this.objectPath = objectPath;
        }

        public PlugRef(String busName, String objectPath) {
            this(busName, new DBusPath(objectPath));
        }

        public String getBusName() {
            return busName;
        }

        public DBusPath getObjectPath() {
            return objectPath;
        }
    }
}
