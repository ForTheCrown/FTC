package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.useables.Usable;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

abstract class InteractableNode<H extends Usable> extends FtcCommand {
    protected String argumentName;

    protected InteractableNode(@NotNull String name) {
        super(name);
        this.argumentName = name;

        setPermission(Permissions.USABLES);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        create(command);
    }

    protected void create(LiteralArgumentBuilder<CommandSource> command) {
        var createLiteral = literal("-create");
        createNewUsableArguments(createLiteral);

        command
                .then(createLiteral)
                .then(createEditArguments());
    }

    protected RequiredArgumentBuilder<CommandSource, ?> createEditArguments() {
        var argument = argument("holder_value", getArgumentType());
        UsageHolderProvider<H> provider = context -> get("holder_value", context);

        var removeLiteral = literal("remove");
        createRemoveArguments(removeLiteral, provider);
        addEditArguments(argument, provider);

        return argument
                .then(literal("info")
                        .executes(c -> {
                            var holder = provider.get(c);
                            var writer = TextWriters.newWriter();

                            holder.adminInfo(writer);

                            c.getSource().sendMessage(writer);
                            return 0;
                        })
                )

                .then(literal("data")
                        .executes(c -> {
                            var holder = provider.get(c);
                            CompoundTag tag = new CompoundTag();
                            holder.save(tag);

                            c.getSource().sendMessage(
                                    Text.displayTag(tag, true)
                            );
                            return 0;
                        })
                )

                .then(removeLiteral)

                .then(UsableCommands.CHECK_NODE.createArguments(provider))
                .then(UsableCommands.ACTION_NODE.createArguments(provider));
    }

    protected abstract void createNewUsableArguments(LiteralArgumentBuilder<CommandSource> command);

    protected abstract void createRemoveArguments(LiteralArgumentBuilder<CommandSource> command, UsageHolderProvider<H> provider);

    protected abstract ArgumentType<?> getArgumentType();

    protected abstract H get(String argumentName, CommandContext<CommandSource> context) throws CommandSyntaxException;

    protected void addEditArguments(RequiredArgumentBuilder<CommandSource, ?> command, UsageHolderProvider<H> provider) {
    }
}