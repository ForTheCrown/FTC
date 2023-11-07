package net.forthecrown.antigrief;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import java.time.Duration;
import java.time.Instant;
import lombok.Data;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

/**
 * A single punishment instance given to a player.
 */
@Data
public class Punishment {

  public static final String
      KEY_SOURCE = "source",
      KEY_BEGAN = "began",
      KEY_TYPE = "type",
      KEY_REASON = "reason",
      KEY_EXTRA = "extra",
      KEY_EXPIRES = "expires",
      KEY_PARDON_SOURCE = "pardonSource",
      KEY_PARDON_DATE = "pardonDate";

  public static final long INDEFINITE_EXPIRY = -1;

  /**
   * The name of staff member that created this punishment, in the case of a console-issued
   * punishment, this will be "Server"
   */
  private final String source;

  /**
   * The reason for the punishment, may be null
   */
  private final String reason;

  /**
   * Extra data stored in the punishment. Only used by {@link PunishType#JAIl} to store the name of
   * the jail cell the user was placed into.
   */
  private final String extra;

  /**
   * The punishment's type
   */
  private final PunishType type;

  /**
   * Timestamp of when the punishment was issued
   */
  private final Instant began;

  /**
   * Timestamp of when the punishment expire/when it did expire.
   * <p>
   * If a punishment wasn't given an expiry date, this will be
   * {@link #INDEFINITE_EXPIRY}
   */
  @Nullable
  private final Instant expires;

  /**
   * If this punishment was lifted via a staff pardon, this will be the name of who pardoned it.
   * <p>
   * As was before, this will be "Server" if the pardon came from the console
   */
  private String pardonSource;

  /**
   * The date this punishment was pardoned, if it was pardoned. If this punishment has not been
   * pardoned, this will be <code>-1</code>
   */
  private long pardonDate;

  /**
   * The task which will expire this punishment
   */
  private BukkitTask task;

  /**
   * Starts the punishment expiry task, will not do anything if
   * {@code expires == }{@link #INDEFINITE_EXPIRY}
   *
   * @param callback The callback to call once the punishment expires
   */
  public void startTask(Runnable callback) {
    if (expires == null) {
      return;
    }

    cancelTask();
    Instant now = Instant.now();

    if (now.isAfter(expires)) {
      callback.run();
    }

    Duration until = Duration.between(now, expires);
    Tasks.runLater(callback, until);
  }

  /**
   * True, if this punishment was pardoned by a member of staff or the server console
   *
   * @return True, if this punishment instance was pardoned
   */
  public boolean wasPardoned() {
    return pardonDate != -1 && pardonSource != null;
  }

  /**
   * Function called by {@link PunishEntry} when this punishment type is pardoned.
   *
   * @param pardonSource The name of the staff member pardoning this entry, "Server", if pardon was
   *                     issued by console.
   */
  public void onPardon(String pardonSource) {
    this.pardonSource = pardonSource;
    this.pardonDate = System.currentTimeMillis();
  }

  /**
   * Stops the expiry task
   */
  public void cancelTask() {
    task = Tasks.cancel(task);
  }

  /**
   * Writes info about this punishment into the given writer
   *
   * @param writer The writer to write to
   */
  public void writeDisplay(TextWriter writer) {
    writeField(writer, "Source", source);
    writeField(writer, "Began", Text.formatDate(began));
    writeField(writer, "Type", type.presentableName());

    if (expires != null) {
      writeField(writer, "Expires", Text.formatDate(expires));
    }

    writeField(writer, "Reason", reason);
    writeField(writer, "Extra", extra);

    if (wasPardoned()) {
      writeField(writer, "Pardon date", Text.formatDate(pardonDate));
      writeField(writer, "Pardon source", pardonSource);
    }
  }

  private void writeField(TextWriter writer, String field, @Nullable String val) {
    if (Strings.isNullOrEmpty(val)) {
      return;
    }

    writeField(writer, field, Component.text(val));
  }

  private void writeField(TextWriter writer, String field, @Nullable Component display) {
    if (display == null) {
      return;
    }

    writer.write(Component.text(field + ": ", NamedTextColor.GRAY));
    writer.write(display);
    writer.newLine();
  }

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();

    json.add(KEY_SOURCE, source);
    json.addInstant(KEY_BEGAN, began);
    json.addEnum(KEY_TYPE, type);

    if (!Strings.isNullOrEmpty(reason)) {
      json.add(KEY_REASON, reason);
    }

    if (!Strings.isNullOrEmpty(extra)) {
      json.add(KEY_EXTRA, extra);
    }

    if (expires != null) {
      json.addInstant(KEY_EXPIRES, expires);
    }

    if (wasPardoned()) {
      json.addTimeStamp(KEY_PARDON_DATE, pardonDate);
      json.add(KEY_PARDON_SOURCE, pardonSource);
    }

    return json.getSource();
  }

  public static Punishment read(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    var punishment = new Punishment(
        json.getString(KEY_SOURCE),
        json.getString(KEY_REASON, null),
        json.getString(KEY_EXTRA, null),
        json.getEnum(KEY_TYPE, PunishType.class, PunishType.SOFT_MUTE),
        json.getInstant(KEY_BEGAN, Instant.now()),
        json.getInstant(KEY_EXPIRES, null)
    );

    if (json.has(KEY_PARDON_DATE)) {
      punishment.setPardonDate(json.getTimeStamp(KEY_PARDON_DATE));
      punishment.setPardonSource(json.getString(KEY_PARDON_SOURCE));
    }

    return punishment;
  }
}