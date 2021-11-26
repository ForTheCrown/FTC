package net.forthecrown.commands.admin;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.utils.FtcUtils;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.util.Collection;

public class CommandLaunch extends FtcCommand {

    public CommandLaunch() {
        super("launch");

        setPermission(Permissions.FTC_ADMIN);
        setAliases("rocket");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Launches entities with the given vector :3
     *
     * Valid usages of command:
     * /launch <entities> <vector>
     * /rocket <entities> <vector>
     *
     * Permissions used: ftc.admin
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("entity", EntityArgument.multipleEntities())
                .then(argument("vec", PositionArgument.position())
                        .executes(c -> {
                            Collection<Entity> entities = EntityArgument.getEntities(c, "entity");
                            Position pos = c.getArgument("vec", Position.class);

                            for (Entity e: entities) {
                                Location loc = FtcUtils.locFromPosition(pos, e.getLocation());
                                Vec3 velocity = new Vec3(loc.getX(), loc.getY(), loc.getZ());

                                net.minecraft.world.entity.Entity nmsEnt = ((CraftEntity) e).getHandle();
                                nmsEnt.setDeltaMovement(velocity);
                                nmsEnt.hurtMarked = true;
                            }

                            c.getSource().sendAdmin("Launched " + entities.size() + " entities");
                            return 0;
                        })
                )
        );
    }
}