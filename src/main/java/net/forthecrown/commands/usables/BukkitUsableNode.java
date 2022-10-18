package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.useables.BukkitSavedUsable;
import org.jetbrains.annotations.NotNull;

public abstract class BukkitUsableNode<H extends BukkitSavedUsable> extends InteractableNode<H> {
    public BukkitUsableNode(@NotNull String name) {
        super(name);
    }

    @Override
    protected void addEditArguments(RequiredArgumentBuilder<CommandSource, ?> command, UsageHolderProvider<H> provider) {
        command
                .then(literal("cancelVanilla")
                        .executes(c -> {
                            var holder = provider.get(c);

                            c.getSource().sendMessage(
                                    Text.format("Usage will cancel vanilla interaction: {}",
                                            holder.isCancelVanilla()
                                    )
                            );
                            return 0;
                        })

                        .then(argument("cancellation_state", BoolArgumentType.bool())
                                .executes(c -> {
                                    var holder = provider.get(c);
                                    var state = c.getArgument("cancellation_state", Boolean.class);

                                    holder.setCancelVanilla(state);

                                    c.getSource().sendAdmin(
                                            Text.format("Set usage will cancel vanilla interaction: {0}",
                                                    state
                                            )
                                    );
                                    return 0;
                                })
                        )
                );
    }
}