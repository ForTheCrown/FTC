package net.forthecrown.usables.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.ComponentList;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.objects.Usable;
import net.forthecrown.usables.objects.UsableObject;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class InteractableCommand<H extends Usable> extends UsableCommand<H> {

  public InteractableCommand(String name, String argumentName) {
    super(name, argumentName);
  }

  @Override
  protected void createUsages(UsageFactory factory) {
    super.createUsages(factory);

    factory.usage("silent", "Checks if a usable is silent");
    factory.usage("silent <true | false>", "Sets a usable to be silent or not");

    UsablesCommands.actions.createUsages(factory);
    UsablesCommands.conditions.createUsages(factory);
  }

  @Override
  protected <B extends ArgumentBuilder<CommandSource, B>> void createEditArguments(
      B argument,
      UsableProvider<H> provider
  ) {
    super.createEditArguments(argument, provider);

    argument.then(literal("silent")
        .requires(hasAdminPermission())

        .executes(c -> {
          H holder = provider.get(c);

          c.getSource().sendMessage(
              Text.format("&e{0}&r will send fail messages: &f{1}&r.",
                  NamedTextColor.GRAY,
                  holder.displayName(),
                  !holder.isSilent()
              )
          );
          return 0;
        })

        .then(argument("state", BoolArgumentType.bool())
            .executes(c -> {
              H holder = provider.get(c);
              boolean silent = c.getArgument("state", Boolean.class);

              holder.setSilent(silent);
              provider.postEdit(holder);

              c.getSource().sendSuccess(
                  Text.format("&e{0}&r will now failure messages: &f{1}&r.",
                      NamedTextColor.GRAY,
                      holder.displayName(),
                      !holder.isSilent()
                  )
              );
              return 0;
            })
        )
    );

    argument.then(UsablesCommands.actions.create(context -> {
      H holder = provider.get(context);
      return new ActionListAccess<>(holder, provider);
    }).requires(hasAdminPermission()));

    argument.then(UsablesCommands.conditions.create(context -> {
      H holder = provider.get(context);
      return new ConditionListAccess<>(holder, provider);
    }).requires(hasAdminPermission()));
  }

  record ActionListAccess<T extends Usable>(
      T usable,
      UsableProvider<T> provider
  ) implements ListHolder<Action> {

    @Override
    public ComponentList<Action> getList() {
      return usable.getActions();
    }

    @Override
    public void postEdit() {
      provider.postEdit(usable);
    }

    @Override
    public UsableObject object() {
      return usable;
    }
  }

  record ConditionListAccess<T extends Usable>(
      T usable,
      UsableProvider<T> provider
  ) implements ListHolder<Condition> {

    @Override
    public ComponentList<Condition> getList() {
      return usable.getConditions();
    }

    @Override
    public void postEdit() {
      provider.postEdit(usable);
    }

    @Override
    public UsableObject object() {
      return usable;
    }
  }
}
