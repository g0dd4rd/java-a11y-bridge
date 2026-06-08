package org.a11y.bridge.atspi;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.a11y.Bus")
public interface A11yBusIface extends DBusInterface {

    String GetAddress();
}
