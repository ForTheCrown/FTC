package net.forthecrown.usables.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import net.forthecrown.usables.UPermissions;
import net.forthecrown.usables.UsablesPlugin;
import net.forthecrown.usables.trigger.Trigger;
import net.forthecrown.usables.trigger.Trigger.Type;
import net.forthecrown.usables.trigger.TriggerManager;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.permissions.Permission;

public class UsableTriggerCommand extends InteractableCommand<Trigger> {

  private final TriggerManager manager;

  public UsableTriggerCommand(TriggerManager manager) {
    super("usabletriggers", "triggers");
    setAliases("usable_triggers", "triggers");
    setPermission(UPermissions.TRIGGER);

    this.manager = manager;
  }

  @Override
  public Permission getAdminPermission() {
    return UPermissions.TRIGGER;
  }

  @Override
  protected String usagePrefix() {
    return "<trigger>";
  }

  @Override
  protected void createUsages(UsageFactory factory) {
    factory.usage("rename <trigger name> <new name>")
        .addInfo("Renames a trigger to <name>");

    factory.usage("redefine <trigger name>")
        .addInfo("Redefines a trigger to your current world edit selection");

    factory.usage("remove").addInfo("Removes a trigger");
  }

  @Override
  protected void createPrefixedUsages(UsageFactory factory) {
    factory.usage("create <name>")
        .addInfo("Defines a new trigger, using your world edit")
        .addInfo("selection as the trigger's area");
  }

  @Override
  protected void addPrefixedArguments(LiteralArgumentBuilder<CommandSource> builder) {
    builder.then(literal("define")
        .then(argument("name", Arguments.FTC_KEY)
            .executes(c -> {
              define(c, false);
              return 0;
            })

            // Area input given, use that
            .then(argument("pos1", ArgumentTypes.blockPosition())
                .then(argument("pos2", ArgumentTypes.blockPosition())
                    .executes(c -> {
                      define(c, true);
                      return 0;
                    })
                )
            )
        )
    );
  }

  private void define(CommandContext<CommandSource> c, boolean boundsSet)
      throws CommandSyntaxException
  {
    String name = c.getArgument("name", String.class);
    Trigger existing = manager.get(name);

    if (existing != null) {
      throw Exceptions.alreadyExists("Trigger", existing.displayName());
    }

    WorldBounds3i area;

    if (boundsSet) {
      Location p1 = ArgumentTypes.getLocation(c, "pos1");
      Location p2 = ArgumentTypes.getLocation(c, "pos2");
      area = WorldBounds3i.of(p1, p2);
    } else {
      area = WorldBounds3i.ofPlayerSelection(c.getSource().asPlayer());

      if (area == null) {
        throw Exceptions.NO_REGION_SELECTION;
      }
    }

    Trigger trigger = new Trigger();
    trigger.setName(name);
    trigger.setArea(area);

    manager.add(trigger);

    c.getSource().sendSuccess(
        Text.format("Created trigger called '&e{0}&r", NamedTextColor.GRAY, trigger.displayName())
    );
  }

  @Override
  protected <B extends ArgumentBuilder<CommandSource, B>> void createEditArguments(
      B argument,
      UsableProvider<Trigger> provider
  ) {
    super.createEditArguments(argument, provider);

    argument.then(literal("remove").executes(c -> {
      Trigger trigger = c.getArgument("trigger", Trigger.class);
      manager.remove(trigger);

      c.getSource().sendSuccess(
          Text.format("Removed trigger &e{0}&r.", NamedTextColor.GRAY, trigger.displayName())
      );
      return 0;
    }));

    argument.then(literal("redefine")
        .executes(c -> {
          Trigger trigger = provider.get(c);
          var newArea = WorldBounds3i.ofPlayerSelection(c.getSource().asPlayer());

          if (newArea == null) {
            throw Exceptions.NO_REGION_SELECTION;
          }

          trigger.setArea(newArea);

          c.getSource().sendSuccess(
              Text.format("Redefined trigger &e{0}&r to area &6{1}&r.",
                  NamedTextColor.GRAY,
                  trigger.displayName(), newArea
              )
          );
          return 0;
        })
    );

    argument.then(literal("rename")
        .then(argument("newName", Arguments.FTC_KEY)
            .suggests((context, builder) -> {
              Trigger trigger = provider.get(context);
              return Completions.suggest(builder, trigger.getName());
            })
            .executes(c -> {
              Trigger trigger = provider.get(c);

              var oldName = trigger.getName();
              String name = c.getArgument("newName", String.class);

              var existing = manager.get(name);
              if (existing != null) {
                throw Exceptions.format("Trigger with name {0} already exists",
                    existing.displayName()
                );
              }

              trigger.setName(name);

              c.getSource().sendSuccess(
                  Text.format("Renamed trigger '&e{0}&r' to '&6{1}&r'",
                      NamedTextColor.GRAY,
                      oldName, name
                  )
              );
              return 0;
            })
        )
    );

    argument.then(literal("type")
        .executes(c -> {
          var trigger = provider.get(c);

          c.getSource().sendMessage(
              Text.format("&e{0}&r's type is '&6{1}&r'",
                  trigger.displayName(),
                  trigger.getType().name().toLowerCase()
              )
          );
          return 0;
        })

        .then(argument("type", ArgumentTypes.enumType(Type.class))
            .executes(c -> {
              var trigger = provider.get(c);
              var type = c.getArgument("type", Type.class);

              trigger.setType(type);

              c.getSource().sendSuccess(
                  Text.format("Set &e{0}&r's type to &6{1}&r.",
                      NamedTextColor.GRAY,
                      trigger.displayName(),
                      type.name().toLowerCase()
                  )
              );
              return 0;
            })
        )
    );
  }

  @Override
  protected ArgumentType<?> getArgumentType() {
    return new TriggerArgumentType();
  }

  @Override
  protected UsableProvider<Trigger> getProvider(String argument) {
    return context -> context.getArgument(argument, Trigger.class);
  }

  class TriggerArgumentType implements ArgumentType<Trigger> {

    @Override
    public Trigger parse(StringReader reader) throws CommandSyntaxException {
      final int start = reader.getCursor();

      String ftcKey = Arguments.FTC_KEY.parse(reader);

      var triggers = UsablesPlugin.get().getTriggers();
      Trigger trigger = triggers.get(ftcKey);

      if (trigger == null) {
        reader.setCursor(start);
        throw Exceptions.unknown("Trigger", reader, ftcKey);
      }

      return trigger;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
        CommandContext<S> context,
        SuggestionsBuilder builder
    ) {
      return Completions.suggest(builder, manager.getNames());
    }
  }
}
