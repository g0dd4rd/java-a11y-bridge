package org.a11y.bridge.atspi;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.a11y.atspi.Action")
public interface ActionIface extends DBusInterface {

    int GetNActions();

    @DBusMemberName("GetName")
    String GetActionName(int index);

    @DBusMemberName("GetDescription")
    String GetActionDescription(int index);

    @DBusMemberName("GetKeyBinding")
    String GetActionKeyBinding(int index);

    @DBusMemberName("GetLocalizedName")
    String GetActionLocalizedName(int index);

    boolean DoAction(int index);
}
