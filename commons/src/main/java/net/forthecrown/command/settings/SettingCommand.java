package net.forthecrown.command.settings;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import org.bukkit.permissions.Permission;

@Getter @Setter
public class SettingCommand extends FtcCommand {

  private final Setting setting;
  private final Permission othersPermission;

  public SettingCommand(
      String name,
      Setting setting,
      Permission permission,
      Permission othersPermission,
      String... aliases
  ) {
    super(name);

    setPermission(permission);
    setAliases(aliases);
    setDescription("Toggles your " + setting.getDisplayName());

    this.setting = setting;
    this.othersPermission = othersPermission;
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(context -> {
          var user = getUserSender(context);
          setting.toggleState(user);
          return 0;
        })

        .then(argument("user", Arguments.USER)
            .requires(source -> source.hasPermission(othersPermission))

            .executes(c -> {
              User user = Arguments.getUser(c, "user");
              setting.toggleOther(c.getSource(), user);
              return 0;
            })
        );
  }
}