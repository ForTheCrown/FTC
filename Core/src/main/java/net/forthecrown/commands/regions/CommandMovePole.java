package net.forthecrown.commands.regions;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.RegionUtil;
import org.bukkit.Location;

public class CommandMovePole extends FtcCommand {

    public CommandMovePole() {
        super("movepole");

        setAliases("movepost");
        setPermission(Permissions.REGIONS_ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Moves a pole within a region
     *
     * Valid usages of command:
     * /movepole <2d block cords>
     * /movepost <2d block cords>
     *
     * Permissions used: ftc.regions.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("vec", PositionArgument.blockPos2D())
                        .executes(c -> {
                            Location loc = PositionArgument.getLocation(c, "vec");
                            BlockVector2 vec2 = BlockVector2.at(loc.getX(), loc.getZ());

                            PopulationRegion region = Crown.getRegionManager().get(RegionPos.of(vec2));
                            if(!RegionUtil.isValidPolePosition(region, vec2)) {
                                throw FtcExceptionProvider.create("Invalid position for pole, too close to the edge");
                            }

                            region.setPolePosition(vec2);

                            c.getSource().sendAdmin(
                                    "Moved pole of region " +
                                    (region.hasName() ? region.getName() : region.getPos().toString()) +
                                    " to " + vec2.toString()
                            );
                            return 0;
                        })
                );
    }
}