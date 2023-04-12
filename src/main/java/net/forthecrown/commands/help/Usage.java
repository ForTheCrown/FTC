package net.forthecrown.commands.help;

import com.google.common.base.Strings;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.permissions.Permission;

/**
 * A simple piece of data that represents a single usage text for a command.
 */
@Getter
@RequiredArgsConstructor
public class Usage {

  /**
   * The usage argument, example: <code>/pay (user) (amount: number)</code>
   */
  private final String arguments;

  /**
   * Info to represent the above arguments, each array value representing a
   * single line of info
   */
  private String[] info = new String[0];

  /**
   * The condition that must be passed for this usage to be displayed to command
   * sources.
   * <p>
   * By default, just always returns true
   */
  @Setter
  @Accessors(chain = true)
  private Predicate<CommandSource> condition = source -> true;

  public Usage setPermission(Permission permission) {
    return setCondition(source -> source.hasPermission(permission));
  }

  public Usage setPermission(String permission) {
    return setCondition(source -> source.hasPermission(permission));
  }

  public Usage addInfo(String info, Object... args) {
    this.info = ArrayUtils.add(this.info, info.formatted(args));
    return this;
  }

  public String argumentsWithPrefix(String prefix) {
    return prefix + (Strings.isNullOrEmpty(arguments) ? "" : " " + arguments);
  }
}