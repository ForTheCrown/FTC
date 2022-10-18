package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.text.Text;
import net.forthecrown.useables.UsableTrigger;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.Location;

class TriggerNode extends InteractableNode<UsableTrigger> {
    protected TriggerNode() {
        super("area_triggers");
        this.argumentName = "triggers";

        setAliases("usable_triggers", "triggers");
    }

    @Override
    protected void createNewUsableArguments(LiteralArgumentBuilder<CommandSource> command) {
        command
                .then(argument("name", Arguments.FTC_KEY)
                        // No area input given -> use WorldEdit region
                        .executes(c -> {
                            User user = getUserSender(c);
                            com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(user.getPlayer());

                            Region region = Util.getSelectionSafe(wePlayer);
                            WorldBounds3i bounds3i = WorldBounds3i.of(user.getWorld(), region);

                            return createTrigger(c, bounds3i);
                        })

                        // Area input given, use that lol
                        .then(argument("pos1", PositionArgument.blockPos())
                                .then(argument("pos2", PositionArgument.blockPos())
                                        .executes(c -> {
                                            getUserSender(c);

                                            Location p1 = PositionArgument.getLocation(c, "pos1");
                                            Location p2 = PositionArgument.getLocation(c, "pos2");

                                            WorldBounds3i bounds = WorldBounds3i.of(p1, p2);

                                            return createTrigger(c, bounds);
                                        })
                                )
                        )
                );
    }

    private int createTrigger(CommandContext<CommandSource> c, WorldBounds3i area) throws CommandSyntaxException {
        String name = c.getArgument("name", String.class);
        var triggers = Usables.get().getTriggers();

        if (triggers.contains(name)) {
            throw Exceptions.alreadyExists("Trigger", name);
        }

        UsableTrigger trigger = new UsableTrigger(name, area);
        triggers.add(trigger);

        c.getSource().sendAdmin("Created trigger called: " + name);
        return 0;
    }

    @Override
    protected void createRemoveArguments(LiteralArgumentBuilder<CommandSource> command, UsageHolderProvider<UsableTrigger> provider) {
        command
                .executes(c -> {
                    var trigger = provider.get(c);
                    var triggers = Usables.get().getTriggers();

                    triggers.remove(trigger);

                    c.getSource().sendAdmin(
                            Text.format("Removed trigger named '{}'", trigger.getName())
                    );
                    return 0;
                });
    }

    @Override
    protected ArgumentType<?> getArgumentType() {
        return Arguments.TRIGGER;
    }

    @Override
    protected UsableTrigger get(String argumentName, CommandContext<CommandSource> context) throws CommandSyntaxException {
        return context.getArgument(argumentName, UsableTrigger.class);
    }
}