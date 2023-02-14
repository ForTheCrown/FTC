package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.useables.TriggerType;
import net.forthecrown.useables.UsableTrigger;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.text.Text;
import org.bukkit.Location;

class TriggerNode extends InteractableNode<UsableTrigger> {

  protected TriggerNode() {
    super("area_triggers");
    this.argumentName = "triggers";

    setAliases("usable_triggers", "triggers");
  }

  @Override
  protected UsageFactory prefixWithType(UsageFactory factory) {
    return factory.withPrefix("<trigger name>");
  }

  @Override
  protected void addCreateUsage(UsageFactory factory) {
    factory.usage("<name>")
        .addInfo("Creates a trigger from your currently selected WorldEdit")
        .addInfo("selection");

    factory.usage("<name> <pos1: x,y,z> <pos2: x,y,z>")
        .addInfo("Creates a trigger from the area between <pos1> and <pos2>");
  }

  @Override
  protected void addUsages(UsageFactory factory) {
    factory.usage("type")
        .addInfo("Shows a trigger's type");

    factory.usage("type <type>")
        .addInfo("Sets a trigger's type to <type>");

    super.addUsages(factory);
  }

  @Override
  protected UsableSaveCallback<UsableTrigger> saveCallback() {
    return UsableSaveCallback.empty();
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

  private int createTrigger(CommandContext<CommandSource> c, WorldBounds3i area)
      throws CommandSyntaxException {
    String name = c.getArgument("name", String.class);
    var triggers = Usables.getInstance().getTriggers();

    if (triggers.contains(name)) {
      throw Exceptions.alreadyExists("Trigger", name);
    }

    UsableTrigger trigger = new UsableTrigger(name, area);
    triggers.add(trigger);

    c.getSource().sendAdmin("Created trigger called: " + name);
    return 0;
  }

  @Override
  protected void createRemoveArguments(LiteralArgumentBuilder<CommandSource> command,
                                       UsageHolderProvider<UsableTrigger> provider
  ) {
    command
        .executes(c -> {
          var trigger = provider.get(c);
          var triggers = Usables.getInstance().getTriggers();

          triggers.remove(trigger);

          c.getSource().sendAdmin(
              Text.format("Removed trigger named '{0}'", trigger.getName())
          );
          return 0;
        });
  }

  @Override
  protected void addEditArguments(RequiredArgumentBuilder<CommandSource, ?> command,
                                  UsageHolderProvider<UsableTrigger> provider
  ) {
    command
        .then(literal("type")
            .executes(c -> {
              var trigger = provider.get(c);

              c.getSource().sendMessage(
                  Text.format("{0}'s type is '{1}'",
                      trigger.getName(),
                      trigger.getType().name().toLowerCase()
                  )
              );
              return 0;
            })

            .then(argument("type", EnumArgument.of(TriggerType.class))
                .executes(c -> {
                  var trigger = provider.get(c);
                  var type = c.getArgument("type", TriggerType.class);

                  trigger.setType(type);

                  c.getSource().sendAdmin(
                      Text.format("Set {0}'s type to {1}",
                          trigger.getName(),
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
    return Arguments.TRIGGER;
  }

  @Override
  protected UsableTrigger get(String argumentName, CommandContext<CommandSource> context)
      throws CommandSyntaxException {
    return context.getArgument(argumentName, UsableTrigger.class);
  }
}