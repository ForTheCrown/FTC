package net.forthecrown.commands.usables;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EntitySelector;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.Usables;
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
  protected UsageFactory prefixWithType(UsageFactory factory) {
    return factory.withPrefix("<entity: selector>");
  }

  @Override
  protected void createNewUsableArguments(LiteralArgumentBuilder<CommandSource> command) {
    command
        .then(argument("entity", ArgumentTypes.entity())
            .executes(c -> {
              CommandSource source = c.getSource();
              Entity entity = c.getArgument("entity", EntitySelector.class)
                  .findEntity(source);

              if (entity instanceof Player) {
                throw Exceptions.PLAYER_USABLE;
              }

              if (Usables.getInstance().isUsableEntity(entity)) {
                throw Exceptions.ALREADY_USABLE_ENTITY;
              }

              Usables.getInstance().createEntity(entity);

              c.getSource().sendSuccess(text("Creating usable entity"));
              return 0;
            })
        );
  }

  @Override
  protected void createRemoveArguments(LiteralArgumentBuilder<CommandSource> command,
                                       UsageHolderProvider<UsableEntity> provider
  ) {
    command
        .executes(c -> {
          var holder = provider.get(c);
          Usables.getInstance().deleteEntity(holder);

          c.getSource().sendSuccess(text("Deleted usable entity"));
          return 0;
        });
  }

  @Override
  protected ArgumentType<?> getArgumentType() {
    return ArgumentTypes.entity();
  }

  @Override
  protected UsableEntity get(String argumentName, CommandContext<CommandSource> context)
      throws CommandSyntaxException {
    var entity = ArgumentTypes.getEntity(context, argumentName);
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

          text("[" + command + "]", NamedTextColor.AQUA)
              .hoverEvent(text("Click to run"))
              .clickEvent(ClickEvent.runCommand(command))
      );
    }

    return usables.getEntity(entity);
  }
}