package net.forthecrown.sellshop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class UserShopData implements UserComponent, Iterable<UserShopData.Entry> {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final String KEY_AUTO_SELL = "autoSelling";
  private static final String KEY_EARNED = "earned";
  private static final String KEY_LAST_DROP = "last_earnings_drop";

  /**
   * An empty entry array
   */
  public static final Entry[] EMPTY_ARRAY = new Entry[0];

  /**
   * Array of earning entries.
   * <p>
   * The index of entries in this array corresponds to a given entry's {@link Material#ordinal()}
   */
  private Entry[] earnings = EMPTY_ARRAY;

  /**
   * The set of materials this user is auto selling
   */
  @Getter
  private final EnumSet<Material> autoSelling = EnumSet.noneOf(Material.class);

  private Instant lastEarningsDrop;

  private final User user;

  public UserShopData(User user) {
    this.user = user;
  }

  /**
   * Adds the given amount to the earnings of the given material
   *
   * @param material The material to add to the value of
   * @param value    The amount to add
   * @see #set(Material, int)
   */
  public void add(Material material, int value) {
    set(material, get(material) + value);
  }

  /**
   * Sets the amount of rhines earned from the given material
   * <p>
   * If the given value is less than or equal to 0, this will remove the given material instead of
   * setting its value
   *
   * @param material The material to set the value of
   * @param value    The amount of rhines earned from the given material
   */
  public void set(Material material, int value) {
    // If remove
    if (value <= 0) {
      remove(material);
      return;
    }

    int index = material.ordinal();
    earnings = ObjectArrays.ensureCapacity(earnings, index + 1);

    var entry = earnings[index];

    if (entry == null) {
      entry = new Entry(material);
      earnings[index] = entry;
    }

    entry.value = value;
  }

  /**
   * Gets the amount of rhines earned from the given material
   *
   * @param material The material to get the earnings of
   * @return The amount of rhines earned from the given material
   */
  public int get(Material material) {
    if (material.ordinal() >= earnings.length) {
      return 0;
    }

    var entry = earnings[material.ordinal()];

    if (entry == null) {
      return 0;
    }

    return entry.value;
  }

  /**
   * Removes all earnings from the given material
   *
   * @param material The material to remove the earnings of
   */
  public void remove(Material material) {
    int index = material.ordinal();

    if (index >= earnings.length) {
      return;
    }

    earnings[index] = null;
  }

  /**
   * Clears the earnings of this user. This does not clear the auto sell map
   */
  public void clear() {
    Arrays.fill(earnings, null);
  }

  /**
   * Tests if the earning data is empty
   *
   * @return True, if this user has no recorded earnings, false otherwise
   */
  public boolean isEmpty() {
    return !iterator().hasNext();
  }

  public void onLogin() {
    int drop = getEarningsDrop();

    if (drop <= 0) {
      return;
    }

    LOGGER.info("Lowering {}'s earnings by {}", user, drop);

    lastEarningsDrop = Instant.now();

    var it = iterator();
    while (it.hasNext()) {
      var entry = it.next();
      entry.value = entry.value - drop;

      if (entry.value <= 0) {
        it.remove();
      }
    }
  }

  private int getEarningsDrop() {
    var config = SellShopPlugin.getPlugin().getShopConfig();
    var interval = config.earningLossInterval();

    Instant lastReset;

    if (lastEarningsDrop == null) {
      long lastJoin = user.getTime(TimeField.LAST_LOGIN);
      lastReset = Instant.ofEpochMilli(lastJoin);
    } else {
      lastReset = lastEarningsDrop;
    }

    Instant now = Instant.now();
    Instant nextReset = lastReset.plus(interval);

    if (now.isBefore(nextReset)) {
      return 0;
    }

    Duration between = Duration.between(lastEarningsDrop, nextReset);

    long betweenMillis = between.toMillis();
    long intervalMillis = interval.toMillis();

    long divided = betweenMillis / intervalMillis;

    return (int) (config.earningLoss() * divided);
  }

  @Override
  public void deserialize(@Nullable JsonElement element) {
    clear();
    autoSelling.clear();

    if (element == null) {
      return;
    }

    var json = JsonWrapper.wrap(element.getAsJsonObject());

    if (json.has(KEY_EARNED)) {
      for (var e : json.getObject(KEY_EARNED).entrySet()) {
        var material = Material.matchMaterial(e.getKey());

        if (material == null) {
          LOGGER.warn("Found unknown material in earnings JSON of user {}: material: '{}'",
              user, e.getKey()
          );

          continue;
        }

        add(material, e.getValue().getAsInt());
      }
    }

    if (json.has(KEY_AUTO_SELL)) {
      autoSelling.addAll(
          json.getList(KEY_AUTO_SELL, element1 -> JsonUtils.readEnum(Material.class, element1))
      );
    }

    if (json.has(KEY_LAST_DROP)) {
      lastEarningsDrop = json.getInstant(KEY_LAST_DROP, null);
    }
  }

  @Override
  public @Nullable JsonElement serialize() {
    var json = JsonWrapper.create();
    var it = iterator();

    // If the iterator returns false for this within
    // just the first call, it means the array is empty
    if (it.hasNext()) {
      var earned = JsonWrapper.create();

      while (it.hasNext()) {
        var next = it.next();

        earned.add(next.material.name().toLowerCase(), next.value);
      }

      json.add(KEY_EARNED, earned);
    }

    if (!autoSelling.isEmpty()) {
      var autoSell = new JsonArray();

      for (var e : autoSelling) {
        autoSell.add(JsonUtils.writeEnum(e));
      }

      json.add(KEY_AUTO_SELL, autoSell);
    }

    if (lastEarningsDrop != null) {
      json.addInstant(KEY_LAST_DROP, lastEarningsDrop);
    }

    return json.nullIfEmpty();
  }

  @Override
  public ArrayIterator<Entry> iterator() {
    return ArrayIterator.unmodifiable(earnings);
  }

  @Getter
  @Setter
  @RequiredArgsConstructor
  public static class Entry {

    private final Material material;
    private int value;
  }
}