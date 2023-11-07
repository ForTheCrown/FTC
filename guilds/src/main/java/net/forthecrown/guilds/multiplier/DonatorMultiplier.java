package net.forthecrown.guilds.multiplier;

import com.google.gson.JsonElement;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonWrapper;

@Getter
public class DonatorMultiplier {
  
  private static final String
      KEY_UUID = "donatorId",
      KEY_DURATION = "duration",
      KEY_MODIFIER = "modifier",
      KEY_ACTIVATION_TIME = "activationTime",
      KEY_REMAINING = "remainingMillis",
      KEY_TYPE = "type";

  public static final long NOT_ACTIVATED = -1L;

  /* ------------------------------ MEMBERS ------------------------------- */

  private final UUID donator;
  private final float modifier;
  private final long duration;

  private final MultiplierType type;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  private long activationTime = NOT_ACTIVATED;

  @Getter @Setter
  private long remainingMillis;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  public DonatorMultiplier(UUID donator,
                           float modifier,
                           long duration,
                           MultiplierType type
  ) {
    this.donator = donator;
    this.modifier = modifier;
    this.duration = duration;
    this.type = type;
    this.remainingMillis = duration;
  }

  /* ------------------------------ METHODS ------------------------------- */

  public void forEachAffected(Consumer<User> consumer) {
    type.forEachAffected(this, consumer);
  }

  public boolean appliesTo(UUID uuid) {
    return type.appliesTo(this, uuid);
  }

  public boolean isActive() {
    return getActivationTime() != NOT_ACTIVATED
        && getRemainingMillis() > 0;
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();
    json.addUUID(KEY_UUID, donator);
    json.add(KEY_DURATION, duration);
    json.add(KEY_MODIFIER, modifier);
    json.addEnum(KEY_TYPE, type);
    json.add(KEY_REMAINING, remainingMillis);

    if (activationTime != NOT_ACTIVATED) {
      json.addTimeStamp(KEY_ACTIVATION_TIME, activationTime);
    }

    return json.getSource();
  }

  public static DonatorMultiplier deserialize(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    UUID uuid = json.getUUID(KEY_UUID);
    float mod = json.getFloat(KEY_MODIFIER);
    MultiplierType type = json.getEnum(KEY_TYPE, MultiplierType.class);

    long duration = json.getLong(KEY_DURATION);
    long activationTime = json.getLong(KEY_ACTIVATION_TIME, NOT_ACTIVATED);
    long remaining = json.getLong(KEY_REMAINING, duration);

    DonatorMultiplier multiplier
        = new DonatorMultiplier(uuid, mod, duration, type);

    multiplier.setActivationTime(activationTime);
    multiplier.setRemainingMillis(remaining);

    return multiplier;
  }
}