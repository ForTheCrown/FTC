package net.forthecrown.waypoints.menu;

import com.google.common.base.Strings;
import java.time.Instant;
import java.util.Comparator;
import lombok.Getter;
import net.forthecrown.menu.Slot;
import net.forthecrown.text.Text;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.WaypointProperty;

public enum WaypointOrder implements Comparator<Waypoint> {

  NAME (Slot.of(8, 1)) {
    @Override
    public int compare(Waypoint o1, Waypoint o2) {
      String n1 = Strings.nullToEmpty(o1.getEffectiveName());
      String n2 = Strings.nullToEmpty(o2.getEffectiveName());
      return n1.compareToIgnoreCase(n2);
    }
  },

  CREATION_DATE (Slot.of(8, 2)) {
    @Override
    public int compare(Waypoint o1, Waypoint o2) {
      Instant i1 = o1.getCreationTime();
      Instant i2 = o2.getCreationTime();

      if (i1 == i2 && i1 == null) {
        return 0;
      } if (i1 == null) {
        return 1;
      } else if (i2 == null) {
        return -1;
      }

      return i2.compareTo(i1);
    }
  },

  MONTHLY_VISITS (Slot.of(8, 3)) {
    @Override
    public int compare(Waypoint o1, Waypoint o2) {
      return compareProp(o1, o2, WaypointProperties.VISITS_MONTHLY);
    }
  },

  TOTAL_VISITS (Slot.of(8, 4)) {
    @Override
    public int compare(Waypoint o1, Waypoint o2) {
      return compareProp(o1, o2, WaypointProperties.VISITS_TOTAL);
    }
  };

  @Getter
  private final Slot slot;
  private final Comparator<Waypoint> reversed;

  WaypointOrder(Slot slot) {
    this.slot = slot;
    this.reversed = Comparator.super.reversed();
  }

  @Override
  public Comparator<Waypoint> reversed() {
    return reversed;
  }

  public String displayName() {
    return Text.prettyEnumName(this);
  }

  static <T extends Comparable<T>> int compareProp(
      Waypoint o1,
      Waypoint o2,
      WaypointProperty<T> property
  ) {
    T v1 = o1.get(property);
    T v2 = o2.get(property);

    if (v1 == null || v2 == null) {
      return 0;
    }

    return v2.compareTo(v1);
  }
}
