package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.Usables;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

class UsableEntityNode extends BukkitUsableNode<UsableEntity> {

    public UsableEntityNode() {
        super("usable_entity");
        this.argumentName = "entity";
    }

    @Override
    protected void createNewUsableArguments(LiteralArgumentBuilder<CommandSource> command) {
        command
                .then(argument("entity", EntityArgument.entity())
                        .executes(c -> {
                            CommandSource source = c.getSource();
                            Entity entity = c.getArgument("entity", EntitySelector.class)
                                    .getEntity(source);

                            if (entity instanceof Player) {
                                throw Exceptions.PLAYER_USABLE;
                            }

                            if (Usables.getInstance().isUsableEntity(entity)) {
                                throw Exceptions.ALREADY_USABLE_ENTITY;
                            }

                            Usables.getInstance().createEntity(entity);

                            c.getSource().sendAdmin("Creating usable entity");
                            return 0;
                        })
                );
    }

    @Override
    protected void createRemoveArguments(LiteralArgumentBuilder<CommandSource> command, UsageHolderProvider<UsableEntity> provider) {
        command
                .executes(c -> {
                    var holder = provider.get(c);
                    Usables.getInstance().deleteEntity(holder);

                    c.getSource().sendAdmin("Deleted usable entity");
                    return 0;
                });
    }

    @Override
    protected ArgumentType<?> getArgumentType() {
        return EntityArgument.entity();
    }

    @Override
    protected UsableEntity get(String argumentName, CommandContext<CommandSource> context) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, argumentName);
        var usables = Usables.getInstance();

        if (entity instanceof Player) {
            throw Exceptions.PLAYER_USABLE;
        }

        if (!usables.isUsableEntity(entity)) {
            String input = Commands.findInput(argumentName, context);

            if (input == null) {
                throw Exceptions.ENTITY_NOT_USABLE;
            }

            String command = "/usable_entity -create " + input;

            throw Exceptions.format(
                    "Entity {0} is not usable!\n{1} to make it usable",
                    entity.teamDisplayName(),

                    Component.text("[" + command + "]", NamedTextColor.AQUA)
                            .hoverEvent(Component.text("Click to run"))
                            .clickEvent(ClickEvent.runCommand(command))
            );
        }

        return usables.getEntity(entity);
    }
}