package net.forthecrown.text.format;

import static net.forthecrown.text.UnitFormat.UNIT_GEM;
import static net.forthecrown.text.UnitFormat.UNIT_RHINE;
import static net.kyori.adventure.text.Component.text;

import java.time.Duration;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.PeriodFormat;
import net.forthecrown.text.Text;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Time;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class TextFormatTypes {

  public static final TextFormatType DEFAULT = (value, style) -> Text.valueOf(value);

  /**
   * Formats the given <code>arg</code> input into a rhines message.
   * <p>
   * If the argument is null or not a number, then it will call {@link Text#valueOf(Object)}
   */
  public static final TextFormatType RHINES = unitFormatter(UNIT_RHINE);

  /**
   * Same as {@link #RHINES} except it returns the argument formatted with Gems as the unit
   * instead of Rhines.
   */
  public static final TextFormatType GEMS = unitFormatter(UNIT_GEM);

  /**
   * Formats the given {@link Date}/{@link Number} <code>arg</code> into a date component
   * <p>
   * Style:<pre>
   * - If the style is empty, this will use
   *   {@link Text#DATE_FORMAT} to format the given
   *   time into a date.
   *
   * - Otherwise, the given style is treated as a
   *   {@link java.text.DateFormat} format and that
   *   will be used to format the given date
   * </pre>
   * <p>
   * If the argument is null or not a long timestamp or date, then it will call
   * {@link Text#valueOf(Object)}
   */
  public static final TextFormatType DATE = new DateFormatType();

  /**
   * Formats the given <code>arg</code> as a class to get its name.
   * <p>
   * Style:
   * <pre>
   * - If the style is empty or contains the '-simple'
   *   argument, then the returned text will use the
   *   class' simple name.
   *
   * - If the style contains the '-long- argument,
   *   then the returned text will use the class'
   *   full name
   * </pre>
   * <p>
   * This will test if the given argument is itself a class, or is a regular object, if the
   * <code>arg</code> is a class, it uses that, if it's a regular object, it uses the class of
   * that object.
   * <p>
   * If the arg is null, then {@link Text#valueOf(Object)} is returned instead
   */
  public static final TextFormatType CLASS = (value, style) -> {
    if (value == null) {
      return Text.valueOf(null);
    }

    Class c = value instanceof Class<?> ? (Class) value : value.getClass();
    boolean simple = style.isBlank() || style.contains("-simple");

    return text(
        simple ? c.getSimpleName() : c.getName()
    );
  };

  /**
   * Formats a <code>arg</code> into a time component using {@link PeriodFormat}. An example of
   * this type's output might be: '4 days, 3 hours and 14 seconds'
   * <p>
   * Style: <pre>
   * - If the style is empty then the formatter will simply
   *   return the given number with {@link PeriodFormat#of(long)}
   *
   * - If the style contains the '-biggest' argument, then
   *   this will call {@link PeriodFormat#retainBiggest()}
   *   before returning the formatted result
   *
   * - If the style contains the '-timestamp' argument,
   *   then the argument will be treated as a time stamp
   *   and the time value will be calculated as the
   *   difference between the given timestamp and the
   *   current time
   *
   * - If the style contains the '-short' argument,
   *   all time units will be reduced to 1 letter,
   *   eg: '1 second' -> '1s'
   *
   * - If the style contains the '-ticks' argument,
   *   the inputted argument will be treated like a tick
   *   value instead of a millisecond value
   * </pre>
   * <p>
   * If the given argument is not a number or is null, {@link Text#valueOf(Object)} is returned
   * instead.
   */
  public static final TextFormatType TIME = (value, style) -> {
    long time;

    if (value instanceof Duration duration) {
      time = duration.toMillis();
    } else if (value instanceof Number number) {
      time = number.longValue();
    } else {
      return Text.valueOf(value);
    }

    if (style.contains("-ticks")) {
      time = Time.ticksToMillis(time);
    }

    PeriodFormat format;

    if (style.contains("-timestamp")) {
      format = PeriodFormat.timeStamp(time);
    } else {
      format = PeriodFormat.of(time);
    }

    if (style.contains("-biggest")) {
      format = format.retainBiggest();
    }

    if (style.contains("-short")) {
      format = format.withShortNames();
    }

    return format.asComponent();
  };

  /**
   * Formats a given <code>arg</code> into a user's display name.
   * <p>
   * Style: <pre>
   * - If the style is blank. then this will use
   *   {@link User#displayName()} to format the
   *   display name.
   *
   * - If the style contains the '-realName' argument,
   *   then this will use {@link User#displayName()} to
   *   format the display name.
   * </pre>
   * <p>
   * This argument accepts a {@link User}, {@link UUID}, {@link OfflinePlayer} and a
   * {@link CommandSource} as valid argument types.
   * <p>
   * If the given argument is invalid or null, it calls {@link Text#valueOf(Object)} for the given
   * argument
   */
  public static TextFormatType USER = (value, style) -> {
    boolean nickname = style.isBlank() || !style.contains("-realName");
    User user;

    if (value instanceof User user1) {
      user = user1;
    } else if (value instanceof OfflinePlayer player) {
      user = Users.get(player);
    } else if (value instanceof UUID uuid) {
      user = Users.get(uuid);
    } else if (value instanceof CommandSource source) {
      if (!source.isPlayer()) {
        return source.displayName();
      }

      user = Users.get(source.asPlayerOrNull());
    } else {
      return Text.valueOf(value);
    }

    Set<NameRenderFlags> flags = EnumSet.noneOf(NameRenderFlags.class);

    if (nickname) {
      flags.add(NameRenderFlags.ALLOW_NICKNAME);
    }

    return user.displayName(null, flags);
  };

  /**
   * Formats the given <code>arg</code> argument into an item's display name.
   * <p>
   * Style: <pre>
   * - If the style is blank or contains the arg
   *   '-amount', then the returned display name
   *   will have the item's amount prepended onto
   *   the text
   *
   * - If the style contains the '-!amount' arg
   *   then the amount will not be prepended onto
   *   the display name
   * </pre>
   * <p>
   * If the item is not an {@link ItemStack} or is null, this will call
   * {@link Text#valueOf(Object)}
   */
  public static TextFormatType ITEM = (value, style) -> {
    // Make sure we're given an item stack
    // If not, just return a default value
    if (!(value instanceof ItemStack item)) {
      return Text.valueOf(value);
    }

    // Format name with or without the item quantity
    // prepended onto it.
    boolean withAmount = style.isBlank() || !style.contains("-!amount");
    return withAmount ? Text.itemAndAmount(item) : Text.itemDisplayName(item);
  };

  public static final String DEFAULT_NAME = "default";
  public static final Registry<TextFormatType> formatTypes;

  static {
    formatTypes = Registries.newRegistry();

    formatTypes.register(DEFAULT_NAME, DEFAULT);

    formatTypes.register("rhines",  RHINES);
    formatTypes.register("gems",    GEMS);
    formatTypes.register("date",    DATE);
    formatTypes.register("class",   CLASS);
    formatTypes.register("time",    TIME);
    formatTypes.register("user",    USER);
    formatTypes.register("item",    ITEM);
  }

  static TextFormatType unitFormatter(String unit) {
    return new UnitType(unit);
  }

}