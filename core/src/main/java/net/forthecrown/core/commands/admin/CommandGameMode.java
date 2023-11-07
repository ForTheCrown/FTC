package net.forthecrown.core.commands.admin;

import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.user.User;
import org.bukkit.GameMode;
import org.bukkit.permissions.Permission;

public class CommandGameMode extends FtcCommand {

  public CommandGameMode() {
    super("gm");

    setPermission(CorePermissions.CMD_GAMEMODE);
    setAliases("gamemode");
    setDescription("Sets your or another player's gamemode");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory = factory.withPrefix("<game mode>");
    factory.usage("")
        .addInfo("Sets your game mode to <game mode>");

    factory.usage("<player>")
        .addInfo("Sets a <player>'s game mode to <game mode>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("gamemode", ArgumentTypes.gameMode())
            .executes(c -> {
              User user = getUserSender(c);
              GameMode gameMode = c.getArgument("gamemode", GameMode.class);

              if (!user.hasPermission(gamemodePermission(gameMode))) {
                throw Exceptions.NO_PERMISSION;
              }

              user.setGameMode(gameMode);
              sendGameModeMessages(c.getSource(), user, gameMode);
              return 0;
            })

            .then(argument("user", Arguments.ONLINE_USER)
                .requires(s -> s.hasPermission(CorePermissions.CMD_GAMEMODE_OTHERS))

                .executes(c -> {
                  var source = c.getSource();
                  User user = Arguments.getUser(c, "user");
                  GameMode gameMode = c.getArgument("gamemode", GameMode.class);

                  user.setGameMode(gameMode);
                  sendGameModeMessages(source, user, gameMode);
                  return 0;
                })
            )
        );
  }

  static void sendGameModeMessages(CommandSource source, User target, GameMode mode) {
    // If self, only tell sender they changed their game mode
    if (target.getName().equals(source.textName())) {
      target.sendMessage(CoreMessages.gameModeChangedSelf(mode));
    } else {
      // If the target user cannot see the admin broadcast that
      // their game mode was changed
      if (!target.hasPermission(gamemodePermission(mode))) {
        target.sendMessage(CoreMessages.gameModeChangedTarget(source, mode));
      }

      source.sendSuccess(CoreMessages.gameModeChangedOther(target, mode));
    }
  }

  static Permission gamemodePermission(GameMode mode) {
    return switch (mode) {
      case CREATIVE -> CorePermissions.CMD_GAMEMODE_CREATIVE;
      case ADVENTURE -> CorePermissions.CMD_GAMEMODE_ADVENTURE;
      case SPECTATOR -> CorePermissions.CMD_GAMEMODE_SPECTATOR;
      case SURVIVAL -> CorePermissions.CMD_GAMEMODE;
    };
  }
}