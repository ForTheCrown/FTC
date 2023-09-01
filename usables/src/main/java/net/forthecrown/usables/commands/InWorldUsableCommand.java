package net.forthecrown.usables.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.usables.objects.InWorldUsable;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class InWorldUsableCommand<H extends InWorldUsable> extends InteractableCommand<H> {

  public InWorldUsableCommand(String name, String argumentName) {
    super(name, argumentName);
  }

  @Override
  protected <B extends ArgumentBuilder<CommandSource, B>> void createEditArguments(
      B argument,
      UsableProvider<H> provider
  ) {
    super.createEditArguments(argument, provider);

    argument.then(literal("cancel_vanilla")
        .executes(c -> {
          H holder = provider.get(c);

          c.getSource().sendMessage(
              Text.format("&e{0}&r cancels vanilla interactions: &f{1}&r.",
                  NamedTextColor.GRAY,
                  holder.displayName(),
                  holder.isCancelVanilla()
              )
          );
          return 0;
        })

        .then(argument("state", BoolArgumentType.bool())
            .executes(c -> {
              H holder = provider.get(c);
              boolean state = c.getArgument("state", Boolean.class);

              holder.setCancelVanilla(!state);
              provider.postEdit(holder);

              c.getSource().sendMessage(
                  Text.format("&e{0}&r now cancels vanilla interactions: &f{1}&r.",
                      NamedTextColor.GRAY,
                      holder.displayName(),
                      holder.isCancelVanilla()
                  )
              );
              return 0;
            })
        )
    );

  }
}
