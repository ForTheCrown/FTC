package net.forthecrown.guilds;

import java.util.Comparator;
import lombok.Getter;
import net.forthecrown.menu.Slot;

@Getter
public enum DiscoverySort implements Comparator<Guild> {
  BY_NAME("Guild name", Slot.of(8, 1)) {
    @Override
    public int compare(Guild o1, Guild o2) {
      return o1.getName().compareTo(o2.getName());
    }
  },

  BY_AGE("Guild age", Slot.of(8, 2)) {
    @Override
    public int compare(Guild o1, Guild o2) {
      return Long.compare(
          o1.getCreationTimeStamp(),
          o2.getCreationTimeStamp()
      );
    }
  },

  BY_MEMBERS("#members", Slot.of(8, 3)) {
    @Override
    public int compare(Guild o1, Guild o2) {
      return Integer.compare(
          o2.getMemberSize(),
          o1.getMemberSize()
      );
    }
  },

  BY_EXP("Total Guild Exp", Slot.of(8, 4)) {
    @Override
    public int compare(Guild o1, Guild o2) {
      return Long.compare(o2.getTotalExp(), o1.getTotalExp());
    }
  };

  /**
   * Ascending order arrow
   */
  public static final String ASC_ARROW = "▲";

  /**
   * Descending order arrow
   */
  public static final String DES_ARROW = "▼";

  private final String name;
  private final Slot slot;

  DiscoverySort(String name, Slot slot) {
    this.name = "Sort by " + name;
    this.slot = slot;
  }

  @Override
  public Comparator<Guild> reversed() {
    return (o1, o2) -> this.compare(o2, o1);
  }
}