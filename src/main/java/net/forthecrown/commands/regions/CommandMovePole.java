package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.Regions;
import org.bukkit.Location;
import org.spongepowered.math.vector.Vector2i;

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
                            Vector2i vec2 = new Vector2i(loc.getX(), loc.getZ());

                            PopulationRegion region = RegionManager.get().get(RegionPos.of(vec2));

                            if (!Regions.isValidPolePosition(region, vec2)) {
                                throw Exceptions.BAD_POLE_POSITION;
                            }

                            region.setPolePosition(vec2.equals(region.getPos().toCenter()) ? null : vec2);

                            c.getSource().sendAdmin(
                                    "Moved pole of region " +
                                    (region.hasName() ? region.getName() : region.getPos().toString()) +
                                    " to " + vec2
                            );
                            return 0;
                        })
                );
    }
}