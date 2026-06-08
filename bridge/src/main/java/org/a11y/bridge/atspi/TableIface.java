package org.a11y.bridge.atspi;

import org.a11y.bridge.atspi.AccessibleIface.AccessibleRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

import java.util.List;

@DBusInterfaceName("org.a11y.atspi.Table")
public interface TableIface extends DBusInterface {

    AccessibleRef GetAccessibleAt(int row, int column);

    int GetIndexAt(int row, int column);

    int GetRowAtIndex(int index);

    int GetColumnAtIndex(int index);

    String GetRowDescription(int row);

    String GetColumnDescription(int column);

    int GetRowExtentAt(int row, int column);

    int GetColumnExtentAt(int row, int column);

    AccessibleRef GetRowHeader(int row);

    AccessibleRef GetColumnHeader(int column);

    List<Integer> GetSelectedRows();

    List<Integer> GetSelectedColumns();

    boolean IsRowSelected(int row);

    boolean IsColumnSelected(int column);

    boolean IsSelected(int row, int column);

    boolean AddRowSelection(int row);

    boolean AddColumnSelection(int column);

    boolean RemoveRowSelection(int row);

    boolean RemoveColumnSelection(int column);
}
