package org.a11y.bridge.atspi;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.a11y.atspi.Value")
public interface ValueIface extends DBusInterface {

    double GetCurrentValue();

    double GetMinimumValue();

    double GetMaximumValue();

    double GetMinimumIncrement();

    boolean SetCurrentValue(double value);
}
