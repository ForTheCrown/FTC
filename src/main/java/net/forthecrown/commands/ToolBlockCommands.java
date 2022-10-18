package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class ToolBlockCommands extends FtcCommand {
    private final UtilityBlockFunction function;

    private ToolBlockCommands(String name,
                              Permission selfPerm,
                              UtilityBlockFunction function,
                              String description,
                              String... aliases
    ){
        super(name);

        this.aliases = aliases;
        this.function = function;
        this.permission = selfPerm;
        this.description = description;

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> function.run(c.getSource(), getUserSender(c), true));
    }

    public interface UtilityBlockFunction {
        int run(CommandSource source, User user, boolean self);
    }

    public static void createCommands() {
        new ToolBlockCommands("enderchest",
                Permissions.ENDER_CHEST,
                (source, user, self) -> {
                    Player player = user.getPlayer();
                    player.openInventory(player.getEnderChest());

                    return 0;
                },
                "Opens your Ender Chest",
                "ec", "echest"
        );

        new ToolBlockCommands("workbench",
                Permissions.WORKBENCH,
                (source, user, self) -> {
                    user.getPlayer().openWorkbench(null, true);
                    return 0;
                },
                "Opens a workbench",
                "wb", "craftingtable"
        );

        new ToolBlockCommands("stonecutter",
                Permissions.STONE_CUTTER,
                (source, user, self) -> {
                    user.getPlayer().openStonecutter(null, true);
                    return 0;
                },
                "Opens the stone cutter menu"
        );

        new ToolBlockCommands("grindstone",
                Permissions.GRINDSTONE,
                (source, user, self) -> {
                    user.getPlayer().openGrindstone(null, true);

                    return 0;
                },
                "Opens the grindstone menu"
        );

        new ToolBlockCommands("cartography",
                Permissions.CARTOGRAPHY,
                (source, user, self) -> {
                    user.getPlayer().openCartographyTable(null, true);
                    return 0;
                },
                "Opens a cartography table"
        );

        new ToolBlockCommands("smithingtable",
                Permissions.SMITHING,
                (source, user, self) -> {
                    user.getPlayer().openSmithingTable(null, true);
                    return 0;
                },
                "Opens the smithing table",
                "smithing"
        );

        new ToolBlockCommands("loom",
                Permissions.LOOM,
                (source, user, self) -> {
                    user.getPlayer().openLoom(null, true);
                    return 0;
                },
                "Opens the loom inventory"
        );
    }
}