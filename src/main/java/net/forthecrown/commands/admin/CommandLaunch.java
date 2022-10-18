package net.forthecrown.commands.admin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class CommandLaunch extends FtcCommand {

    public CommandLaunch() {
        super("launch");

        setPermission(Permissions.ADMIN);
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
                .executes(c -> {
                    Player player = c.getSource().asPlayer();
                    Vector dir = player.getLocation().getDirection();

                    return launch(c, new Vec3(dir.getX(), dir.getY(), dir.getZ()));
                })

                .then(argument("vec", PositionArgument.position())
                        .executes(c -> {
                            Position pos = c.getArgument("vec", Position.class);

                            if (pos.isXRelative() || pos.isYRelative() || pos.isZRelative()) {
                                throw Exceptions.CANNOT_USE_RELATIVE_CORD;
                            }

                            return launch(c, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                        })
                )
        );
    }

    int launch(CommandContext<CommandSource> c, Vec3 velocity) throws CommandSyntaxException {
        Collection<Entity> entities = EntityArgument.getEntities(c, "entity");

        for (Entity e: entities) {
            net.minecraft.world.entity.Entity nmsEnt = ((CraftEntity) e).getHandle();
            nmsEnt.setDeltaMovement(velocity);
            nmsEnt.hurtMarked = true;
        }

        c.getSource().sendAdmin("Launched " + entities.size() + " entities");
        return 0;
    }
}