package org.a11y.bridge.atspi;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

import java.util.List;
import java.util.Map;

@DBusInterfaceName("org.a11y.atspi.Accessible")
public interface AccessibleIface extends DBusInterface {

    // Methods that return simple types (no struct unpacking issue)
    int GetIndexInParent();

    List<RelationEntry> GetRelationSet();

    UInt32 GetRole();

    String GetRoleName();

    String GetLocalizedRoleName();

    List<UInt32> GetState();

    Map<String, String> GetAttributes();

    List<String> GetInterfaces();

    // Returns array of structs — dbus-java handles structs inside containers correctly
    List<AccessibleRef> GetChildren();

    // Returns (so) — dbus-java will unpack to s,o but let's see if clients tolerate it
    AccessibleRef GetChildAtIndex(int index);

    AccessibleRef GetApplication();

    // Struct types used in properties and array returns
    class AccessibleRef extends Struct {
        @Position(0)
        private final String busName;
        @Position(1)
        private final DBusPath objectPath;

        public AccessibleRef(String busName, DBusPath objectPath) {
            this.busName = busName;
            this.objectPath = objectPath;
        }

        public AccessibleRef(String busName, String objectPath) {
            this(busName, new DBusPath(objectPath));
        }

        public String getBusName() {
            return busName;
        }

        public DBusPath getObjectPath() {
            return objectPath;
        }
    }

    class RelationEntry extends Struct {
        @Position(0)
        private final UInt32 type;
        @Position(1)
        private final List<AccessibleRef> targets;

        public RelationEntry(UInt32 type, List<AccessibleRef> targets) {
            this.type = type;
            this.targets = targets;
        }

        public UInt32 getType() {
            return type;
        }

        public List<AccessibleRef> getTargets() {
            return targets;
        }
    }
}
