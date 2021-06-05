package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.useables.Usable;
import net.forthecrown.emperor.useables.UsableEntity;
import net.forthecrown.emperor.useables.UsableSign;
import net.forthecrown.emperor.utils.InterUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CommandInteractable extends FtcCommand {

    public CommandInteractable(){
        super("interactable", CrownCore.inst());

        setAliases("usable");
        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("sign")
                        .then(argument("location", PositionArgument.position())
                                .then(literal("create")
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Location l = c.getArgument("location", Position.class).getLocation(source);

                                            if(!(l.getBlock().getState() instanceof Sign)) throw FtcExceptionProvider.create("Block is not sign");
                                            if(CrownCore.getUsablesManager().isInteractableSign(l.getBlock())) throw FtcExceptionProvider.create("Block is already an interactable sign");

                                            CrownCore.getUsablesManager().createSign((Sign) l.getBlock().getState());
                                            c.getSource().sendAdmin("Creating interactable sign");
                                            return 0;
                                        })
                                )

                                .then(editArg(this::getSign))
                                .then(removeArg(this::getSign))
                        )
                )
                .then(literal("entity")
                        .then(argument("selector", EntityArgument.entity())
                                .then(literal("create")
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Entity entity = c.getArgument("selector", EntitySelector.class).getEntity(source);

                                            if(entity instanceof Player) throw FtcExceptionProvider.create("Players cannot be interactable");
                                            if(CrownCore.getUsablesManager().isInteractableEntity(entity)) throw FtcExceptionProvider.create("Entity is already interactable");

                                            CrownCore.getUsablesManager().createEntity(entity);

                                            c.getSource().sendAdmin("Creating interactable entity");
                                            return 0;
                                        })
                                )

                                .then(editArg(this::entity))
                                .then(removeArg(this::entity))
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSource> editArg(InterUtils.BrigadierFunction<Usable> supplier){
        return literal("edit")
                .then(InterUtils.actionsArguments(supplier::apply))
                .then(InterUtils.checksArguments(supplier::apply))

                .then(literal("sendFail")
                        .then(argument("bool", BoolArgumentType.bool())
                                .executes(c -> {
                                    Usable sign = supplier.apply(c);
                                    boolean bool = c.getArgument("bool", Boolean.class);

                                    sign.setSendFail(bool);

                                    c.getSource().sendAdmin("Interaction check will send failure messages: " + bool);
                                    return 0;
                                })
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSource> removeArg(InterUtils.BrigadierFunction<Usable> supplier){
        return literal("remove")
                .executes(c -> {
                    Usable interactable = supplier.apply(c);

                    interactable.delete();

                    c.getSource().sendAdmin("Deleting Interactable");
                    return 0;
                });
    }

    private UsableEntity entity(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Entity entity = c.getArgument("selector", EntitySelector.class).getEntity(c.getSource());
        if(!CrownCore.getUsablesManager().isInteractableEntity(entity)) throw FtcExceptionProvider.create("Given entity is not an interactable entity");

        return CrownCore.getUsablesManager().getEntity(entity);
    }

    private UsableSign getSign(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Location l = c.getArgument("location", Position.class).getLocation(c.getSource());
        Block b = l.getBlock();
        if(!CrownCore.getUsablesManager().isInteractableSign(b)) throw FtcExceptionProvider.create("Specified location is not an interactable sign");

        return CrownCore.getUsablesManager().getSign(b.getLocation());
    }
}
