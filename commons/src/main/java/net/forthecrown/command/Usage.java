package net.forthecrown.command;

import com.google.common.base.Strings;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.permissions.Permission;

@Getter
@Accessors(chain = true)
public class Usage {

  private final String arguments;

  private String[] info;

  @Setter
  private Predicate<CommandSource> condition;

  public Usage(String arguments) {
    this.arguments = arguments;
  }

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