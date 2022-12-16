package net.forthecrown.commands.admin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
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

                    return launch(c, dir, false);
                })

                .then(argument("vec", PositionArgument.position())
                        .executes(c -> launchVelocityGiven(c, false))
                )

                .then(literal("add")
                        .then(argument("vec", PositionArgument.position())
                                .executes(c -> launchVelocityGiven(c, false))
                        )
                )
        );
    }

    int launchVelocityGiven(CommandContext<CommandSource> c,
                            boolean add
    ) throws CommandSyntaxException {
        Position pos = c.getArgument("vec", Position.class);

        if (pos.isXRelative() || pos.isYRelative() || pos.isZRelative()) {
            throw Exceptions.CANNOT_USE_RELATIVE_CORD;
        }

        return launch(
                c,
                new Vector(pos.getX(), pos.getY(), pos.getZ()),
                add
        );
    }

    int launch(CommandContext<CommandSource> c,
               Vector velocity,
               boolean add
    ) throws CommandSyntaxException {
        Collection<Entity> entities = EntityArgument.getEntities(c, "entity");

        for (Entity e: entities) {
            if (add) {
                e.setVelocity(e.getVelocity().add(velocity.clone()));
            } else {
                e.setVelocity(velocity.clone());
            }
        }

        c.getSource().sendAdmin("Launched " + entities.size() + " entities");
        return 0;
    }
}