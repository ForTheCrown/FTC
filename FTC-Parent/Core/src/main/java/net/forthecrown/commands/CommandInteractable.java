package net.forthecrown.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.InteractionUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CommandInteractable extends FtcCommand {

    public CommandInteractable(){
        super("interactable", ForTheCrown.inst());

        setAliases("usable");
        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("block")
                        .then(argument("location", PositionArgument.blockPos())
                                .then(literal("create")
                                        .executes(c -> {
                                            CommandSource source = c.getSource();
                                            Location l = c.getArgument("location", Position.class).getLocation(source);

                                            if(!(l.getBlock().getState() instanceof TileState)) throw FtcExceptionProvider.create("Block is not sign");
                                            if(ForTheCrown.getUsablesManager().isInteractableSign(l.getBlock())) throw FtcExceptionProvider.create("Block is already an interactable sign");

                                            ForTheCrown.getUsablesManager().createBlock((TileState) l.getBlock().getState());
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
                                            if(ForTheCrown.getUsablesManager().isInteractableEntity(entity)) throw FtcExceptionProvider.create("Entity is already interactable");

                                            ForTheCrown.getUsablesManager().createEntity(entity);

                                            c.getSource().sendAdmin("Creating interactable entity");
                                            return 0;
                                        })
                                )

                                .then(editArg(this::entity))
                                .then(removeArg(this::entity))
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSource> editArg(InteractionUtils.BrigadierFunction<Usable> supplier){
        return literal("edit")
                .then(InteractionUtils.actionsArguments(supplier::apply))
                .then(InteractionUtils.checksArguments(supplier::apply))

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

    private LiteralArgumentBuilder<CommandSource> removeArg(InteractionUtils.BrigadierFunction<Usable> supplier){
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
        if(!ForTheCrown.getUsablesManager().isInteractableEntity(entity)) throw FtcExceptionProvider.create("Given entity is not an interactable entity");

        return ForTheCrown.getUsablesManager().getEntity(entity);
    }

    private UsableBlock getSign(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Location l = c.getArgument("location", Position.class).getLocation(c.getSource());
        Block b = l.getBlock();
        if(!ForTheCrown.getUsablesManager().isInteractableSign(b)) throw FtcExceptionProvider.create("Specified location is not an interactable sign");

        return ForTheCrown.getUsablesManager().getBlock(b.getLocation());
    }
}
