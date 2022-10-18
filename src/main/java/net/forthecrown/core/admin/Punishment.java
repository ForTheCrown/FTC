package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import lombok.Data;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.utils.*;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;
import static net.forthecrown.user.data.UserTimeTracker.UNSET;

/**
 * A single punishment instance bestowed upon a player.
 * <p>
 * Bestowed is perhaps too generous of a word lol
 */
@Data
public final class Punishment implements JsonSerializable {

    // --- JSON DATA KEYS ---

    public static final String
            KEY_SOURCE = "source",
            KEY_BEGAN = "began",
            KEY_TYPE = "type",
            KEY_REASON = "reason",
            KEY_EXTRA = "extra",
            KEY_EXPIRES = "expires",
            KEY_PARDON_SOURCE = "pardonSource",
            KEY_PARDON_DATE = "pardonDate";

    // --- INSTANCE FIELDS ---

    /**
     * The name of staff member that created this
     * punishment, in the case of a console-issued
     * punishment, this will be "Server"
     */
    private final String source;

    /**
     * The reason for the punishment, may be
     * null
     */
    private final String reason;

    /**
     * Extra data stored in the punishment.
     * Only used by {@link PunishType#JAIL} to
     * store the name of the jail cell the user
     * was placed into.
     */
    private final String extra;

    /**
     * The punishment's type
     */
    private final PunishType type;

    /**
     * Timestamp of when the punishment was issued
     */
    private final long began;

    /**
     * Timestamp of when the punishment expire/when
     * it did expire.
     * <p>
     * If a punishment wasn't given an expiry date,
     * this will be {@link Punishments#INDEFINITE_EXPIRY}
     */
    private final long expires;

    /**
     * If this punishment was lifted via a staff pardon,
     * this will be the name of who pardoned it.
     * <p>
     * As was before, this will be "Server" if the
     * pardon came from the console
     */
    private String pardonSource;

    /**
     * The date this punishment was pardoned, if
     * it was pardoned. If this punishment has not
     * been pardoned, this will be <code>-1</code>
     */
    private long pardonDate;

    /**
     * The task which will expire this punishment
     */
    private BukkitTask task;

    // --- METHODS ---

    /**
     * Starts the punishment expiry task, will not do
     * anything if {@code expires == {@link Punishments#INDEFINITE_EXPIRY}}
     * @param callback The callback to call once the punishment expires
     */
    public void startTask(Runnable callback) {
        if (expires == INDEFINITE_EXPIRY) {
            return;
        }

        cancelTask();

        long until = Time.timeUntil(expires);

        // Until being less than or equal to 0 means
        // that this task was supposed to be executed
        // some time in the past, so run it now lol
        if (until <= 0) {
            callback.run();
        }

        Tasks.runLater(callback, Time.millisToTicks(until));
    }

    /**
     * True, if this punishment was pardoned by
     * a member of staff or the server console
     * @return True, if this punishment instance was pardoned
     */
    public boolean wasPardoned() {
        return pardonDate != UNSET && pardonSource != null;
    }

    /**
     * Function called by {@link PunishEntry} when this
     * punishment type is pardoned.
     *
     * @param pardonSource The name of the staff member
     *                     pardoning this entry, "Server",
     *                     if pardon was issued by console.
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
     * Writes info about this punishment
     * into the given writer
     * @param writer The writer to write to
     */
    public void writeDisplay(TextWriter writer) {
        writeField(writer, "Source", source);
        writeField(writer, "Began", Text.formatDate(began));
        writeField(writer, "Type", type.presentableName());

        if (expires != INDEFINITE_EXPIRY) {
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
        if (Util.isNullOrBlank(val)) {
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

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.create();

        json.add(KEY_SOURCE, source);
        json.addTimeStamp(KEY_BEGAN, began);
        json.addEnum(KEY_TYPE, type);

        if (!Util.isNullOrBlank(reason)) {
            json.add(KEY_REASON, reason);
        }

        if (!Util.isNullOrBlank(extra)) {
            json.add(KEY_EXTRA, extra);
        }

        if (expires != INDEFINITE_EXPIRY) {
            json.addTimeStamp(KEY_EXPIRES, expires);
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
                json.getDate(KEY_BEGAN).getTime(),
                json.getTimeStamp(KEY_EXPIRES, INDEFINITE_EXPIRY)
        );

        if (json.has(KEY_PARDON_DATE)) {
            punishment.setPardonDate(json.getTimeStamp(KEY_PARDON_DATE, UNSET));
            punishment.setPardonSource(json.getString(KEY_PARDON_SOURCE));
        }

        return punishment;
    }
}