package net.forthecrown.guilds;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.IntUnaryOperator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Announcer;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.math.GenericMath;

@Getter
@RequiredArgsConstructor
public class ExpModifiers implements IntUnaryOperator {
  public static final String
      KEY_WEEKEND_MOD = "weekendModifierActive",
      KEY_DONATOR_MODS = "donatorMultipliers",
      KEY_UUID = "donatorId",
      KEY_ENDTIME = "endTime",
      KEY_MODIFIER = "modifier";

  private final List<DonatorMultiplier> multipliers = new ObjectArrayList<>();

  public void addMultiplier(UUID donator, float modifier, Duration duration) {
    long millis = duration.toMillis();
    long endTime = System.currentTimeMillis() + millis;

    DonatorMultiplier multiplier = new DonatorMultiplier(
        donator, modifier, endTime
    );

    addMultiplier(multiplier);
  }

  public void addMultiplier(DonatorMultiplier multiplier) {
    if (Time.isPast(multiplier.getEndTime())) {
      return;
    }

    long until = Time.millisToTicks(Time.timeUntil(multiplier.getEndTime()));

    multipliers.add(multiplier);
    multiplier.task = Tasks.runLater(() -> multipliers.remove(multiplier), until);

    var announcer = Announcer.get();

    // Only 1 multiplier active now
    if (multipliers.size() == 1) {
      announcer.announce(Messages.multiplierNowActive(getModifier()));
    }
    // More than 1 active
    else {
      announcer.announce(Messages.multiplierIncremented(getModifier()));
    }
  }

  public boolean isWeekend() {
    var date = LocalDate.now();

    return date.getDayOfWeek() == DayOfWeek.SUNDAY
        || date.getDayOfWeek() == DayOfWeek.SATURDAY;
  }

  @Override
  public int applyAsInt(int operand) {
    float mod = getModifier();

    if (GenericMath.floor(mod) <= 1) {
      return operand;
    }

    return GenericMath.floor(((float) operand) * mod);
  }

  public float getModifier() {
    float result = 0F;

    if (isWeekend()) {
      result += GuildConfig.weekendModifier;
    }

    for (var d: multipliers) {
      result += d.getModifier();
    }

    return Math.min(result, GuildConfig.maxExpMultiplier);
  }

  public void clear() {
    multipliers.forEach(m -> Tasks.cancel(m.getTask()));
    multipliers.clear();

  }

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();
    if (!multipliers.isEmpty()) {
      json.add(KEY_DONATOR_MODS,
          JsonUtils.ofStream(
              multipliers.stream().map(DonatorMultiplier::serialize)
          )
      );
    }
    return json.getSource();
  }

  public void deserialize(JsonWrapper wrapper) {
    clear();

    if (wrapper.has(KEY_DONATOR_MODS)) {
      JsonUtils.stream(wrapper.getArray(KEY_DONATOR_MODS))
          .map(DonatorMultiplier::deserialize)
          .forEach(this::addMultiplier);
    }
  }

  @Getter
  @RequiredArgsConstructor
  @EqualsAndHashCode
  public static class DonatorMultiplier {
    private final UUID donator;
    private final float modifier;
    private final long endTime;

    @EqualsAndHashCode.Exclude
    private BukkitTask task;

    public JsonElement serialize() {
      JsonWrapper json = JsonWrapper.create();
      json.addUUID(KEY_UUID, donator);
      json.addTimeStamp(KEY_ENDTIME, endTime);
      json.add(KEY_MODIFIER, modifier);
      return json.getSource();
    }

    public static DonatorMultiplier deserialize(JsonElement element) {
      JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

      UUID id = json.getUUID(KEY_UUID);
      long endTime = json.getTimeStamp(KEY_ENDTIME);
      float mod = json.getFloat(KEY_MODIFIER);

      return new DonatorMultiplier(id, mod, endTime);
    }
  }
}