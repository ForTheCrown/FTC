package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.manager.FtcCommand.UsageFactory;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.ActionHolder;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.utils.text.Text;

public class UsableCommands extends CmdUtil {

  public static final UsableArgumentNode<UsageTest, CheckHolder> CHECK_NODE
      = new UsableArgumentNode<>(UsageTypeAccessor.CHECKS) {
    @Override
    public void populateUsages(UsageFactory factory, String holderName) {
      factory.usage("silent")
          .addInfo(
              "Shows if the Usage checks of %s will show fail messages",
              holderName
          );

      factory.usage("silent <true | false>")
          .addInfo(
              "Sets if %s will show fail messages or not",
              holderName
          );

      super.populateUsages(factory, holderName);
    }

    @Override
    protected void addExtraArguments(LiteralArgumentBuilder<CommandSource> command,
                                     UsageHolderProvider<? extends CheckHolder> provider,
                                     UsableSaveCallback<? extends CheckHolder> saveCallback
    ) {
      command
          .then(literal("silent")
              .executes(c -> {
                var holder = provider.get(c);

                c.getSource().sendMessage(
                    Text.format("Silent: {0}", holder.isSilent())
                );
                return 0;
              })

              .then(argument("silent_state", BoolArgumentType.bool())
                  .executes(c -> {
                    var holder = provider.get(c);
                    var state = c.getArgument("silent_state", Boolean.class);

                    holder.setSilent(state);
                    saveCallback.dumbHack(holder);

                    c.getSource().sendAdmin(Text.format("Set silent: {0}", state));
                    return 0;
                  })
              )
          );
    }
  };

  public static final UsableArgumentNode<UsageAction, ActionHolder> ACTION_NODE
      = new UsableArgumentNode<>(UsageTypeAccessor.ACTIONS);
}