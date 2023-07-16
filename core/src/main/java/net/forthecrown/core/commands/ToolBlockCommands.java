package net.forthecrown.core.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class ToolBlockCommands extends FtcCommand {

  private final UtilityBlockFunction function;

  private ToolBlockCommands(String name,
                            Permission selfPerm,
                            UtilityBlockFunction function,
                            String description,
                            String... aliases
  ) {
    super(name);

    setAliases(aliases);
    setPermission(selfPerm);
    setDescription(description);
    simpleUsages();

    this.function = function;

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> function.run(c.getSource(), getUserSender(c), true));
  }

  public interface UtilityBlockFunction {

    int run(CommandSource source, User user, boolean self);
  }

  public static void createCommands() {
    new ToolBlockCommands("enderchest",
        CorePermissions.ENDER_CHEST,
        (source, user, self) -> {
          Player player = user.getPlayer();
          player.openInventory(player.getEnderChest());
          user.playSound(Sound.BLOCK_ENDER_CHEST_OPEN, 1, 1);

          return 0;
        },
        "Opens your Ender Chest",
        "ec", "echest"
    );

    new ToolBlockCommands("workbench",
        CorePermissions.WORKBENCH,
        (source, user, self) -> {
          user.getPlayer().openWorkbench(null, true);
          return 0;
        },
        "Opens a workbench",
        "wb", "craftingtable"
    );

    new ToolBlockCommands("stonecutter",
        CorePermissions.STONE_CUTTER,
        (source, user, self) -> {
          user.getPlayer().openStonecutter(null, true);
          return 0;
        },
        "Opens the stone cutter menu"
    );

    new ToolBlockCommands("grindstone",
        CorePermissions.GRINDSTONE,
        (source, user, self) -> {
          user.getPlayer().openGrindstone(null, true);

          return 0;
        },
        "Opens the grindstone menu"
    );

    new ToolBlockCommands("cartography",
        CorePermissions.CARTOGRAPHY,
        (source, user, self) -> {
          user.getPlayer().openCartographyTable(null, true);
          return 0;
        },
        "Opens a cartography table"
    );

    new ToolBlockCommands("smithingtable",
        CorePermissions.SMITHING,
        (source, user, self) -> {
          user.getPlayer().openSmithingTable(null, true);
          return 0;
        },
        "Opens the smithing table",
        "smithing"
    );

    new ToolBlockCommands("loom",
        CorePermissions.LOOM,
        (source, user, self) -> {
          user.getPlayer().openLoom(null, true);
          return 0;
        },
        "Opens the loom inventory"
    );
  }
}