package org.a11y.bridge.atspi;

import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt32;

@DBusInterfaceName("org.a11y.atspi.Component")
public interface ComponentIface extends DBusInterface {

    boolean Contains(int x, int y, UInt32 coordType);

    Extents GetExtents(UInt32 coordType);

    UInt32 GetLayer();

    short GetMDIZOrder();

    boolean GrabFocus();

    double GetAlpha();

    boolean ScrollTo(UInt32 type);

    boolean ScrollToPoint(UInt32 coordType, int x, int y);

    class Extents extends Struct {
        @Position(0)
        private final int x;
        @Position(1)
        private final int y;
        @Position(2)
        private final int width;
        @Position(3)
        private final int height;

        public Extents(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
}
