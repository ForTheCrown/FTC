package net.forthecrown.core.commands.admin;

import java.util.List;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.ExpandedEntityArgument;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;

public class CommandTellRawF extends FtcCommand {

  public CommandTellRawF() {
    super("tellrawf");
    setAliases("tellraw", "ftellraw", "ftc_tellraw");
    setDescription("FTC's version of /tellraw with more lax text input");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("targets", new ExpandedEntityArgument(true, true))
            .then(argument("message", Arguments.CHAT)
                .executes(c -> {
                  ViewerAwareMessage message = Arguments.getMessage(c, "message");
                  List<Entity> entities = ArgumentTypes.getEntities(c, "targets");

                  PlaceholderRenderer renderer = Placeholders.newRenderer().useDefaults();

                  for (Entity entity : entities) {
                    Component rendered = message.create(entity);
                    rendered = renderer.render(rendered, entity);

                    entity.sendMessage(rendered);
                  }

                  return 0;
                })
            )
        );
  }
}
