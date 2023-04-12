package net.forthecrown.commands.help;

import com.google.common.base.Strings;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.permissions.Permission;

/**
 * A function which generates Usage instances
 */
@FunctionalInterface
public interface UsageFactory {

  Usage usage(String arguments);

  default Usage usage(String argument, String... usages) {
    var usage = usage(argument);

    for (var s : usages) {
      usage.addInfo(s);
    }

    return usage;
  }

  default UsageFactory withPrefix(String prefix) {
    return arguments -> usage(
        prefix + (Strings.isNullOrEmpty(arguments) ? "" : " " + arguments)
    );
  }

  default UsageFactory withCondition(Predicate<CommandSource> predicate) {
    return arguments -> usage(arguments).setCondition(predicate);
  }

  default UsageFactory withPermission(Permission permission) {
    return arguments -> usage(arguments).setPermission(permission);
  }

  default UsageFactory withPermission(String permission) {
    return arguments -> usage(arguments).setPermission(permission);
  }
}