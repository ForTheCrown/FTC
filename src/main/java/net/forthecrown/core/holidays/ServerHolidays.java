package net.forthecrown.core.holidays;

import com.google.common.collect.Collections2;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.utils.MonthDayPeriod;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.Logger;

/**
 * The class which manages the server's automated Holiday system.
 */
public class ServerHolidays extends SerializableObject.NbtDat {

  /**
   * The size of the holiday inventory given in shulkers/chests
   */
  public static final int INV_SIZE = 27;

  public static final String
      TAG_NAMESPACE = "holiday",
      TAG_SEPARATOR = ":";

  private static final Logger LOGGER = FTC.getLogger();

  private static final ServerHolidays inst = new ServerHolidays();

  private final Map<String, Holiday> holidays = new Object2ObjectOpenHashMap<>();

  public ServerHolidays() {
    super(PathUtil.pluginPath("holidays.dat"));
  }

  public static ServerHolidays get() {
    return inst;
  }

  @OnDayChange
  void onDayChange(ZonedDateTime time) {
    // Go through each holiday
    for (var h : holidays.values()) {
      // If the holiday has no rewards or is disabled, skip it
      if (h.hasNoRewards() || !h.isEnabled()) {
        continue;
      }

      MonthDayPeriod period = h.getPeriod();

      // If should be active
      if (period.contains(time.toLocalDate())) {
        // It's already active, no need to do anything
        if (h.isActive()) {
          continue;
        }

        // Activate holiday
        h.run();

        // If not exact, set it to be active
        // Needs to be exact so we can later
        // remove items of people who didn't claim
        if (!period.isExact()) {
          LOGGER.info("Set {} to be active", h.getName());
          h.setActive(true);
        }
      } else {
        // Shouldn't be active, and it isn't,
        // no need to disable
        if (period.isExact()) {
          continue;
        }

        // If this is still active when it shouldn't be
        // then turn it off lol
        if (h.isActive()) {
          LOGGER.info("Deactivating {}", h.getName());

          h.setActive(false);
          h.deactivate();
        }
      }
    }
  }

  /**
   * Gets all holidays
   *
   * @return All holidays
   */
  public Collection<Holiday> getAll() {
    return holidays.values();
  }

  /**
   * Adds the given holiday
   *
   * @param holiday The holiday to add
   */
  public void addHoliday(Holiday holiday) {
    holidays.put(holiday.getName().toLowerCase(), holiday);
  }

  /**
   * Removes the given holiday
   *
   * @param holiday The holiday to remove
   */
  public void remove(Holiday holiday) {
    holidays.remove(holiday.getName().toLowerCase());
  }

  /**
   * Gets a holiday by the given name
   *
   * @param name The name of the holiday
   * @return The gotten holiday, null, if none found
   */
  public Holiday getHoliday(String name) {
    return holidays.get(name.toLowerCase());
  }

  /**
   * Gets all the holiday's names
   *
   * @return The holiday names
   */
  public Collection<String> getNames() {
    return Collections2.transform(holidays.values(), Holiday::getName);
  }

  protected void save(CompoundTag tag) {
    for (var e : holidays.entrySet()) {
      CompoundTag hTag = new CompoundTag();
      e.getValue().save(hTag);

      // Save each holiday with the getName() function and
      // not the e.getKey() because that gets toLowerCase()'d
      tag.put(e.getValue().getName(), hTag);
    }
  }

  protected void load(CompoundTag tag) {
    holidays.clear();

    for (var e : tag.tags.entrySet()) {
      // Create holiday and load it, then add it, simple as
      Holiday holiday = new Holiday(e.getKey());
      holiday.load((CompoundTag) e.getValue());

      addHoliday(holiday);
    }
  }
}