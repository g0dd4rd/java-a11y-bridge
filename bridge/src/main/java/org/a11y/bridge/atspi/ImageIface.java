package org.a11y.bridge.atspi;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

@DBusInterfaceName("org.a11y.atspi.Image")
public interface ImageIface extends DBusInterface {

    String GetImageDescription();

    String GetImageLocale();
}
