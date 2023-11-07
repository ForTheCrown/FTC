package net.forthecrown.challenges;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriter;
import net.forthecrown.user.User;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.currency.CurrencyMap;
import net.forthecrown.user.currency.CurrencyMaps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
@Builder(builderClassName = "Builder")
@Data
public class Reward {

  /**
   * Empty reward constant which never rewards anything, like my life lmao
   */
  public static final Reward EMPTY = new Reward(CurrencyMaps.emptyMap());

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  private final CurrencyMap<StreakBasedValue> currencyRewards;

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Tests if the reward is empty
   */
  public boolean isEmpty() {
    return currencyRewards.isEmpty();
  }

  /**
   * Tests if the reward is empty for the given streak value
   */
  public boolean isEmpty(int streak) {
    for (StreakBasedValue value : currencyRewards.values()) {
      int finalValue = value.getInt(streak);
      if (finalValue > 0) {
        return false;
      }
    }

    return true;
  }

  /**
   * Gives rewards to the given user
   *
   * @param user   The user to give rewards to
   * @param streak The user's challenge streak
   */
  public void give(User user, int streak) {

    TextJoiner joiner = TextJoiner.newJoiner()
        .setPrefix(Component.text("You got: ", NamedTextColor.YELLOW))
        .setSuffix(Component.text(".", NamedTextColor.YELLOW))
        .setDelimiter(Component.text(", ", NamedTextColor.YELLOW))
        .setColor(NamedTextColor.GOLD);

    for (Entry<Currency, StreakBasedValue> e : currencyRewards.object2ObjectEntrySet()) {
      Currency c = e.getKey();
      int value = e.getValue().getInt(streak);

      if (value < 1) {
        continue;
      }

      float mod = c.getGainMultiplier(user.getUniqueId());
      int modified = (int) (mod * value);

      c.add(user.getUniqueId(), modified);

      if (mod > 1) {
        joiner.add(
            Text.format(
                "{0} ({1, number}x multiplier)",
                c.format(value), mod
            )
        );
      } else {
        joiner.add(c.format(value));
      }
    }

    user.sendMessage(joiner);
  }

  /**
   * Writes info about this reward to the given writer, using the given streak as context
   */
  public void write(TextWriter writer, int streak, UUID uuid) {
    if (isEmpty()) {
      return;
    }

    writer.field("Rewards", "");

    for (Entry<Currency, StreakBasedValue> e : currencyRewards.object2ObjectEntrySet()) {
      Currency c = e.getKey();
      int value = e.getValue().getInt(streak);

      if (value < 1) {
        continue;
      }

      float mod = c.getGainMultiplier(uuid);

      if (mod > 1) {
        writer.field(c.name(),
            Text.format("{0, number, -floor} ({1, number}x multiplier, {2, number} originally)",
                value * mod, mod, value
            )
        );
      } else {
        writer.field(c.name(), Text.formatNumber(value));
      }
    }
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public static Reward deserialize(JsonElement element) {
    JsonObject obj = element.getAsJsonObject();
    CurrencyMap<StreakBasedValue> map = CurrencyMaps.newMap();

    obj.entrySet().forEach(e -> {
      StreakBasedValue value = StreakBasedValue.read(e.getValue());
      map.putCurrency(e.getKey(), value);
    });

    return new Reward(CurrencyMaps.unmodifiable(map));
  }
}