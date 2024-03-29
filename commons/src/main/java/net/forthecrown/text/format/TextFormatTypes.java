package net.forthecrown.text.format;

import static net.forthecrown.text.UnitFormat.UNIT_GEM;
import static net.forthecrown.text.UnitFormat.UNIT_RHINE;
import static net.kyori.adventure.text.Component.text;

import java.time.Duration;
import java.time.Instant;
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
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.Users;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.user.name.UserNameFactory;
import net.forthecrown.utils.Time;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.math.vector.Vectord;
import org.spongepowered.math.vector.Vectorf;
import org.spongepowered.math.vector.Vectori;
import org.spongepowered.math.vector.Vectorl;

public class TextFormatTypes {

  public static final TextFormatType DEFAULT
      = (value, style, viewer) -> Text.valueOf(value, viewer);

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
  public static final TextFormatType CLASS = (value, style, audience) -> {
    if (value == null) {
      return DEFAULT.resolve(value, style, audience);
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
  public static final TextFormatType TIME = (value, style, audience) -> {
    long time;

    if (value instanceof Duration duration) {
      time = duration.toMillis();
    } else if (value instanceof Number number) {
      time = number.longValue();
    } else if (value instanceof Instant instant) {
      time = instant.toEpochMilli();
    } else {
      return DEFAULT.resolve(value, style, audience);
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
   *
   * - If the style contains '-flat' the returned name
   *   will have no hover event
   * </pre>
   * <p>
   * This argument accepts a {@link User}, {@link UUID}, {@link OfflinePlayer} and a
   * {@link CommandSource} as valid argument types.
   * <p>
   * If the given argument is invalid or null, it calls {@link Text#valueOf(Object)} for the given
   * argument
   */
  public static TextFormatType USER = (value, style, audience) -> {
    boolean nickname = style.isBlank() || !style.contains("-realName");
    boolean flat = style.contains("-flat");

    User user;

    if (value instanceof User user1) {
      user = user1;
    } else if (value instanceof OfflinePlayer player) {
      user = Users.get(player);
    } else if (value instanceof UUID uuid) {
      UserLookup lookup = Users.getService().getLookup();
      var entry = lookup.getEntry(uuid);

      if (entry == null) {
        return Text.valueOf(value, audience);
      }

      user = Users.get(entry);
    } else if (value instanceof CommandSource source) {
      if (!source.isPlayer()) {
        return source.displayName();
      }

      user = Users.get(source.asPlayerOrNull());
    } else {
      return DEFAULT.resolve(value, style, audience);
    }

    Set<NameRenderFlags> flags = EnumSet.noneOf(NameRenderFlags.class);
    DisplayIntent intent;

    if (flat) {
      intent = DisplayIntent.HOVER_TEXT;
    } else {
      intent = DisplayIntent.UNSET;
    }

    if (nickname) {
      flags.add(NameRenderFlags.ALLOW_NICKNAME);
    }

    UserNameFactory factory = Users.getService().getNameFactory();
    var ctx = factory.createContext(user, audience, flags).withIntent(intent);

    return factory.formatDisplayName(user, ctx);
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
  public static TextFormatType ITEM = (value, style, audience) -> {
    // Make sure we're given an item stack
    // If not, just return a default value
    if (!(value instanceof ItemStack item)) {
      return DEFAULT.resolve(value, style, audience);
    }

    // Format name with or without the item quantity
    // prepended onto it.
    boolean withAmount = style.isBlank() || !style.contains("-!amount");
    return withAmount ? Text.itemAndAmount(item) : Text.itemDisplayName(item);
  };

  /**
   * Formats a <code>arg</code> to be a pretty location message.
   * <p>
   * Style: <pre>
   * - If the style is empty, the returned format will
   *   not show the location's world, and will not have
   *   a '/tp_exact' click event.
   *
   * - The '-w' or '-world' argument tells the formatter
   *   to show the location's world.
   * - The '-c' or '-clickable' tells the formatter to
   *   add a click event to the result.
   * </pre>
   * <p>
   * If the given argument is not a {@link Location} or
   * {@link net.forthecrown.utils.math.WorldVec3i} object, or is null, then
   * {@link Text#valueOf(Object)} is returned
   */
  public static final TextFormatType LOCATION = new LocationFormatType();

  /**
   * Formats a given arg to be a vector position. Example of this format's output: 'x23 y64 z89'
   * for a {@link org.spongepowered.math.vector.Vector3i}, For something with more than 4 axes,
   * the result would look like so: '[12, 45, 654, 85, 6545]' I don't know why you'd ever need to
   * use a vector with more than 3 axes, but you can lmao
   * <p>
   * Accepts any form of vector from {@link Vectori}, {@link Vectord}, {@link Vectorf},
   * {@link Vectorl}.
   * <p>
   * If the given argument is null or not one of the above-mentioned vector types,
   * {@link Text#valueOf(Object)} is returned instead
   */
  public static final TextFormatType VECTOR = new VectorFormatType();

  public static final TextFormatType NUMBER = new NumberType();

  public static final String DEFAULT_NAME = "default";
  public static final Registry<TextFormatType> formatTypes;

  static {
    formatTypes = Registries.newRegistry();

    formatTypes.register(DEFAULT_NAME, DEFAULT);

    formatTypes.register("location",  LOCATION);
    formatTypes.register("rhines",    RHINES);
    formatTypes.register("gems",      GEMS);
    formatTypes.register("date",      DATE);
    formatTypes.register("class",     CLASS);
    formatTypes.register("time",      TIME);
    formatTypes.register("user",      USER);
    formatTypes.register("item",      ITEM);
    formatTypes.register("vector",    VECTOR);
    formatTypes.register("number",    NUMBER);
  }

  static TextFormatType unitFormatter(String unit) {
    return new UnitType(unit);
  }

}