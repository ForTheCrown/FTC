package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.function.UnaryOperator;

public class CommandToolBlock extends FtcCommand {
    private final UtilityBlockFunction function;
    private final UnaryOperator<Component> adminMessageFunc;
    private final Permission othersPerm;

    private CommandToolBlock(String name,
                            Permission selfPerm,
                            Permission othersPerm,
                            UnaryOperator<Component> adminMessageFunc,
                            UtilityBlockFunction function,
                            String description,
                            String... aliases
    ){
        super(name, ForTheCrown.inst());

        this.aliases = aliases;
        this.function = function;
        this.permission = selfPerm;
        this.othersPerm = othersPerm;
        this.adminMessageFunc = adminMessageFunc;

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> function.run(c.getSource(), getUserSender(c), true))

                .then(argument("user", UserType.user())
                        .requires(s -> s.hasPermission(othersPerm))

                        .executes(c -> {
                            CrownUser user = UserType.getUser(c, "user");
                            CommandSource source = c.getSource();

                            source.sendAdmin(adminMessageFunc.apply(user.nickDisplayName()));
                            return function.run(source, user, false);
                        })
                );
    }

    public interface UtilityBlockFunction{
        int run(CommandSource source, CrownUser user, boolean self);
    }

    public static void init(){
        new CommandToolBlock("enderchest",
                Permissions.ENDER_CHEST,
                Permissions.ENDER_CHEST_OTHERS,
                c ->  Component.text("Opening enderchest for ").append(c),

                (source, user, self) -> {
                    Player player = user.getPlayer();
                    player.openInventory(player.getEnderChest());

                    return 0;
                }, "Opens your Ender Chest", "ec", "echest"
        );

        new CommandToolBlock("workbench",
                Permissions.WORKBENCH,
                Permissions.WORKBENCH_OTHERS,
                c -> Component.text("Opening workbench for ").append(c),
                (source, user, self) ->{
                    user.getPlayer().openWorkbench(null, true);
                    return 0;
                }, "Opens a workbench","wb", "craftingtable"
        );

        new CommandToolBlock("stonecutter",
                Permissions.STONE_CUTTER,
                Permissions.STONE_CUTTER_OTHERS,
                c -> Component.text("Opening Stone Cutter for ").append(c),
                (source, user, self) -> {
                    user.getPlayer().openStonecutter(null, true);
                    return 0;
                }, "Opens the stone cutter menu"
        );

        new CommandToolBlock("grindstone",
                Permissions.GRINDSTONE,
                Permissions.GRINDSTONE_OTHERS,
                c -> Component.text("Opening Stone Cutter for ").append(c),
                (source, user, self) -> {
                    user.getPlayer().openGrindstone(null, true);

                    return 0;
                }, "Opens the grindstone menu"
        );

        new CommandToolBlock("cartography",
                Permissions.CARTOGRAPHY,
                Permissions.CARTOGRAPHY_OTHERS,
                c -> Component.text("Opening Cartography Table for ").append(c),
                (source, user, self) -> {
                    user.getPlayer().openCartographyTable(null, true);
                    return 0;
                }, "Opens a cartography table"
        );

        new CommandToolBlock("smithingtable",
                Permissions.SMITHING,
                Permissions.SMITHING_OTHERS,
                c -> Component.text("Opening Smithing table for ").append(c),
                (source, user, self) -> {
                    user.getPlayer().openSmithingTable(null, true);
                    return 0;
                }, "Opens the smithing table", "smithing"
        );

        new CommandToolBlock("loom",
                Permissions.LOOM,
                Permissions.LOOM_OTHERS,
                c -> Component.text("Opening loom for ").append(c),
                (source, user, self) -> {
                    user.getPlayer().openLoom(null, true);
                    return 0;
                }, "Opens the loom inventory"
        );
    }
}
