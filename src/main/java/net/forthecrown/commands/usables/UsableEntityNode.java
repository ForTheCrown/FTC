package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.Usables;
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

                            if (Usables.get().isUsableEntity(entity)) {
                                throw Exceptions.ALREADY_USABLE_ENTITY;
                            }

                            Usables.get().createEntity(entity);

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
                    Usables.get().deleteEntity(holder);

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
        var usables = Usables.get();

        if (!usables.isUsableEntity(entity)) {
            throw Exceptions.ENTITY_NOT_USABLE;
        }

        return usables.getEntity(entity);
    }
}