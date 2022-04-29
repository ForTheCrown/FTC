package net.forthecrown.commands.regions;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.utils.FtcUtils;
import org.bukkit.Location;

public class CommandGotoRegion extends FtcCommand {

    public CommandGotoRegion() {
        super("GotoRegion");

        setPermission(Permissions.REGIONS_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Takes the sender to a specific region at
     * either relative region cords or at absolute
     * world cords
     *
     * Valid usages of command:
     * /gotoregion <cords> [absolute | relative]
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("cords", PositionArgument.blockPos2D())
                        .executes(doStuff(true))

                        .then(literal("absolute")
                                .executes(doStuff(true))
                        )
                        .then(literal("relative")
                                .executes(doStuff(false))
                        )
                );
    }

    private Command<CommandSource> doStuff(boolean absolute) {
        return c -> gotoRegion(getUserSender(c), c.getArgument("cords", Position.class), absolute);
    }

    private int gotoRegion(CrownUser user, Position pos, boolean absolute) throws CommandSyntaxException {
        RegionUtil.validateWorld(user.getWorld());
        Location l = FtcUtils.locFromPosition(pos, user.getLocation());

        RegionPos cords;
        if(absolute) cords = RegionPos.of(l);
        else {
            RegionPos temp = user.getRegionPos();

            cords = new RegionPos(
                    pos.isXRelative() ? temp.getX() + pos.getX() : pos.getX(),
                    pos.isZRelative() ? temp.getZ() + pos.getZ() : pos.getZ()
            );
        }

        RegionManager manager = Crown.getRegionManager();
        PopulationRegion region = manager.get(cords);

        ActionFactory.visitRegion(user, region);
        return 0;
    }
}