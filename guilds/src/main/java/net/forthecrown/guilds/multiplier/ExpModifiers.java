package net.forthecrown.guilds.multiplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildMessages;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.math.GenericMath;

@Getter
@RequiredArgsConstructor
public class ExpModifiers {

  public static final int TICK_INTERVAL = 10;

  public static final long MS_INTERVAL
      = Ticks.SINGLE_TICK_DURATION_MS * TICK_INTERVAL;

  /* ------------------------------ MEMBERS ------------------------------- */

  private final GuildManager manager;

  private final ReferenceList<DonatorMultiplier> multiplierList
      = new ReferenceArrayList<>();

  private final ReferenceList<DonatorMultiplier> ticked
      = new ReferenceArrayList<>();

  @Setter
  private float manual;

  private BukkitTask tickingTask;

  /* -------------------------- TICKING CONTROL --------------------------- */

  public void updateTickingState() {
    if (Bukkit.hasWhitelist() || ticked.isEmpty()) {
      Tasks.cancel(tickingTask);
      return;
    }

    if (Tasks.isScheduled(tickingTask)) {
      return;
    }

    tickingTask = Tasks.runTimerAsync(
        this::tick,
        TICK_INTERVAL,
        TICK_INTERVAL
    );
  }

  private void tick() {
    var it = ticked.listIterator();

    while (it.hasNext()) {
      var n = it.next();

      long remaining = n.getRemainingMillis();
      remaining -= MS_INTERVAL;
      n.setRemainingMillis(remaining);

      if (remaining <= 0) {
        it.remove();
        deactivate(n);
      }
    }
  }

  /* ------------------------------ METHODS ------------------------------- */

  public void addMultiplier(UUID donator, float modifier, Duration duration, MultiplierType type) {
    long millis = duration.toMillis();

    DonatorMultiplier multiplier = new DonatorMultiplier(
        donator, modifier, millis, type
    );

    addMultiplier(multiplier);
  }

  public void addMultiplier(DonatorMultiplier multiplier) {
    multiplierList.add(multiplier);

    if (multiplier.getActivationTime() != -1) {
      activate(multiplier);
    }
  }

  public void activate(DonatorMultiplier multiplier) {
    if (multiplier.getActivationTime() == -1) {
      multiplier.setActivationTime(System.currentTimeMillis());
    }

    ticked.add(multiplier);
    updateTickingState();

    if (multiplier.getType() == MultiplierType.GLOBAL) {

    } else {
      var guild = Guilds.getGuild(multiplier.getDonator());

      Objects.requireNonNull(guild,
          "User " + multiplier.getDonator() + " had null guild when "
              + "activating guild multiplier"
      );

      guild.announce(
          Component.text("Guild multiplier activated!", NamedTextColor.YELLOW)
      );
    }

    multiplier.forEachAffected(user -> {
      var active = getActive(user.getUniqueId());
      float mod = getModifier(user.getUniqueId());

      // Only 1 multiplier active now
      if (active.size() == 1) {
        user.sendMessage(GuildMessages.multiplierNowActive(mod));
        return;
      }

      // More than 1 active
      if (active.size() > 1) {
        user.sendMessage(GuildMessages.multiplierIncremented(mod));
      }
    });
  }

  public void deactivate(DonatorMultiplier multiplier) {
    remove(multiplier);

    multiplier.forEachAffected(user -> {
      float mod = getModifier(user.getUniqueId());
      user.sendMessage(GuildMessages.multiplierDecremented(mod));
    });
  }

  public void remove(DonatorMultiplier multiplier) {
    multiplierList.remove(multiplier);
    ticked.remove(multiplier);

    updateTickingState();
  }

  public List<DonatorMultiplier> getMultipliers(UUID uuid,
                                                MultiplierType type
  ) {
    return multiplierList.stream()
        .filter(multiplier -> Objects.equals(uuid, multiplier.getDonator()))
        .filter(multiplier -> multiplier.getType() == type)
        .collect(ObjectArrayList.toList());
  }

  private Collection<DonatorMultiplier> getActive(UUID uuid) {
    return ticked.stream()
        .filter(multiplier -> multiplier.appliesTo(uuid))
        .toList();
  }

  public boolean isWeekend() {
    var date = LocalDate.now();

    return date.getDayOfWeek() == DayOfWeek.SUNDAY
        || date.getDayOfWeek() == DayOfWeek.SATURDAY;
  }

  public int apply(int operand, UUID uuid) {
    float mod = getModifier(uuid);

    if (mod <= 1) {
      return operand;
    }

    return GenericMath.floor(((float) operand) * mod);
  }

  public float getModifier(UUID uuid) {
    float result = getManual();
    var config = manager.getPlugin().getGuildConfig();

    if (isWeekend() && config.weekendMultiplierEnabled) {
      result += config.weekendModifier;
    }

    for (var d: getActive(uuid)) {
      result += d.getModifier();
    }

    return Math.min(result, config.maxExpMultiplier);
  }

  public void clear() {
    multiplierList.clear();
    ticked.clear();
    updateTickingState();
  }

  public JsonObject serialize() {
    JsonWrapper json = JsonWrapper.create();
    json.add("manual", getManual());
    json.add("donator_multipliers",
        JsonUtils.ofStream(
            multiplierList.stream()
                .map(DonatorMultiplier::serialize)
        )
    );

    return json.getSource();
  }

  public void deserialize(JsonElement element) {
    clear();

    if (element.isJsonArray()) {
      loadDonators(element.getAsJsonArray());
      return;
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    setManual(json.getFloat("manual", 0.0F));
    loadDonators(json.getArray("donator_multipliers"));
  }

  private void loadDonators(JsonArray arr) {
    JsonUtils.stream(arr)
        .map(DonatorMultiplier::deserialize)
        .forEach(multiplier -> {
          long remaining = multiplier.getRemainingMillis();

          if (remaining <= 0) {
            return;
          }

          addMultiplier(multiplier);
        });
  }
}