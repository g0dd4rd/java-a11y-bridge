package org.a11y.bridge.atspi;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

import java.util.Map;

@DBusInterfaceName("org.a11y.atspi.Text")
public interface TextIface extends DBusInterface {

    String GetText(int startOffset, int endOffset);

    boolean SetCaretOffset(int offset);

    String GetStringAtOffset(int offset, UInt32 granularity);

    int GetCharacterAtOffset(int offset);

    Map<String, String> GetAttributes(int offset);

    Map<String, String> GetDefaultAttributes();

    int GetNSelections();

    boolean AddSelection(int startOffset, int endOffset);

    boolean RemoveSelection(int selectionNum);

    boolean SetSelection(int selectionNum, int startOffset, int endOffset);

    boolean ScrollSubstringTo(int startOffset, int endOffset, UInt32 type);
}
