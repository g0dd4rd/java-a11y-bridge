package org.a11y.bridge.atspi;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.a11y.atspi.Application")
public interface ApplicationIface extends DBusInterface {

    String GetToolkitName();

    String GetVersion();

    String GetAtspiVersion();

    int GetId();

    String GetLocale(int lctype);
}
