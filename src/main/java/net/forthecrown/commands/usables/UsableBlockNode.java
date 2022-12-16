package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.Usables;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.TileState;

class UsableBlockNode extends BukkitUsableNode<UsableBlock> {
    public UsableBlockNode() {
        super("usable_block");
        argumentName = "block";
    }

    @Override
    protected void createNewUsableArguments(LiteralArgumentBuilder<CommandSource> command) {
        command
                .then(argument("block_pos", getArgumentType())
                        .executes(c -> {
                            CommandSource source = c.getSource();
                            Location l = c.getArgument("block_pos", Position.class)
                                    .getLocation(source);

                            if (!(l.getBlock().getState() instanceof TileState)) {
                                throw Exceptions.USABLE_INVALID_BLOCK;
                            }

                            if (Usables.getInstance().isUsableBlock(l.getBlock())) {
                                throw Exceptions.ALREADY_USABLE_BLOCK;
                            }

                            Usables.getInstance().createBlock(l.getBlock());
                            c.getSource().sendAdmin("Creating usable block");
                            return 0;
                        })
                );
    }

    @Override
    protected void createRemoveArguments(LiteralArgumentBuilder<CommandSource> command, UsageHolderProvider<UsableBlock> provider) {
        command
                .executes(c -> {
                    var holder = provider.get(c);
                    Usables.getInstance().deleteBlock(holder);

                    c.getSource().sendAdmin(
                            Text.format("Removed usable block at {0, location, -clickable -world}",
                                    new WorldVec3i(holder.getWorld(), holder.getPosition())
                            )
                    );
                    return 0;
                });
    }

    @Override
    protected ArgumentType<?> getArgumentType() {
        return PositionArgument.blockPos();
    }

    @Override
    protected UsableBlock get(String argumentName, CommandContext<CommandSource> context) throws CommandSyntaxException {
        var pos = PositionArgument.getLocation(context, argumentName);
        var usables = Usables.getInstance();

        if (!(pos.getBlock().getState() instanceof TileState)) {
            throw Exceptions.USABLE_INVALID_BLOCK;
        }

        if (!usables.isUsableBlock(pos.getBlock())) {
            String input = Commands.findInput(argumentName, context);

            if (input == null) {
                throw Exceptions.BLOCK_NOT_USABLE;
            }

            String command = "/usable_block -create " + input;

            throw Exceptions.format(
                    "{0} is not a usable block!\n{1} to make it a usable",
                    input,

                    Component.text("[" + command + "]", NamedTextColor.AQUA)
                            .hoverEvent(Component.text("Click to run"))
                            .clickEvent(ClickEvent.runCommand(command))
            );
        }

        return usables.getBlock(pos.getBlock());
    }
}