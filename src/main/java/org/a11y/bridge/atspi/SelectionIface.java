package org.a11y.bridge.atspi;

import org.a11y.bridge.atspi.AccessibleIface.AccessibleRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.a11y.atspi.Selection")
public interface SelectionIface extends DBusInterface {

    int GetNSelectedChildren();

    AccessibleRef GetSelectedChild(int index);

    boolean SelectChild(int index);

    boolean DeselectSelectedChild(int index);

    boolean DeselectChild(int index);

    boolean IsChildSelected(int index);

    boolean SelectAll();

    boolean ClearSelection();
}
