package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.text.Text;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.ActionHolder;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageTest;

public class UsableCommands extends CmdUtil {
    public static final UsableArgumentNode<UsageTest, CheckHolder> CHECK_NODE = new UsableArgumentNode<>(UsageTypeAccessor.CHECKS) {
        @Override
        protected void addExtraArguments(LiteralArgumentBuilder<CommandSource> command,
                                         UsageHolderProvider<? extends CheckHolder> provider
        ) {
            command
                    .then(literal("silent")
                            .then(argument("silent_state", BoolArgumentType.bool())
                                    .executes(c -> {
                                        var holder = provider.get(c);
                                        var state = c.getArgument("silent_state", Boolean.class);

                                        holder.setSilent(state);

                                        c.getSource().sendAdmin(Text.format("Set silent: {0}", state));
                                        return 0;
                                    })
                            )
                    );
        }
    };

    public static final UsableArgumentNode<UsageAction, ActionHolder> ACTION_NODE = new UsableArgumentNode<>(UsageTypeAccessor.ACTIONS);
}