package net.forthecrown.commands.regions;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.Regions;
import net.forthecrown.user.User;

import static net.forthecrown.commands.CommandNickname.CLEAR;

public class CommandRenameRegion extends FtcCommand {

    public CommandRenameRegion() {
        super("renameregion");

        setAliases("nameregion");
        setPermission(Permissions.REGIONS_ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /renameregion <name>
     *
     * Permissions used: ftc.regions.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("name", StringArgumentType.word())
                        .suggests(suggestMatching(CLEAR))

                        .then(literal("-paid")
                                .executes(c -> setName(c, true))
                        )

                        .executes(c -> setName(c, false))
                );
    }

    private int setName(CommandContext<CommandSource> c, boolean special) throws CommandSyntaxException {
        User user = getUserSender(c);
        Regions.validateWorld(user.getWorld());

        String name = c.getArgument("name", String.class);

        RegionManager manager = RegionManager.get();
        PopulationRegion region = manager.get(user.getRegionPos());

        if(name.equals(CLEAR)) {
            manager.rename(region, null, false);
            c.getSource().sendAdmin("Removed name of region " + name);
        } else {
            manager.rename(region, name, special);
            c.getSource().sendAdmin("Set name of region to " + name);
        }

        return 0;
    }
}