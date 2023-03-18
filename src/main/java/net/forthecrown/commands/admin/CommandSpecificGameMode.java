package net.forthecrown.commands.admin;

import static net.forthecrown.commands.admin.CommandGameMode.sendGameModeMessages;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import org.bukkit.GameMode;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public class CommandSpecificGameMode extends FtcCommand {

  private final GameMode gameMode;

  private CommandSpecificGameMode(@NotNull String name,
                                  Permission permission,
                                  GameMode mode,
                                  String... aliases
  ) {
    super(name);

    setAliases(aliases);
    this.gameMode = mode;

    setPermission(permission);
    setDescription("Sets your gamemode to " + mode.name().toLowerCase());

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Sets your gamemode to %s", gameMode.name().toLowerCase());

    factory.usage("<player>")
        .addInfo(
            "Sets a <player>'s gamemode to %s",
            gameMode.name().toLowerCase()
        );
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> setGameMode(getUserSender(c), c.getSource()))

        .then(argument("user", Arguments.ONLINE_USER)
            .requires(s -> s.hasPermission(Permissions.CMD_GAMEMODE_OTHERS))

            .executes(c -> setGameMode(
                Arguments.getUser(c, "user"),
                c.getSource()
            ))
        );
  }

  private int setGameMode(User user, CommandSource source) {
    user.setGameMode(gameMode);
    sendGameModeMessages(source, user, gameMode);

    return 0;
  }

  public static void createCommands() {
    new CommandSpecificGameMode("survival", Permissions.CMD_GAMEMODE, GameMode.SURVIVAL, "gms");
    new CommandSpecificGameMode("creative", Permissions.CMD_GAMEMODE_CREATIVE, GameMode.CREATIVE,
        "gmc");
    new CommandSpecificGameMode("spectator", Permissions.CMD_GAMEMODE_SPECTATOR, GameMode.SPECTATOR,
        "gmsp");
    new CommandSpecificGameMode("adventure", Permissions.CMD_GAMEMODE_ADVENTURE, GameMode.ADVENTURE,
        "gma");
  }
}