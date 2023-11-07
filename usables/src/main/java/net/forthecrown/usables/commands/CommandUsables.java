package net.forthecrown.usables.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.usables.UPermissions;
import net.forthecrown.usables.UsablesPlugin;
import net.kyori.adventure.text.Component;

public class CommandUsables extends FtcCommand {

  private final UsablesPlugin plugin;

  public CommandUsables(UsablesPlugin plugin) {
    super("usable");

    setAliases("interactable", "usables");
    setPermission(UPermissions.USABLES);

    this.plugin = plugin;
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    for (UsableCommand cmd : UsablesCommands.usableCommands) {
      UsageFactory prefixed = factory.withPrefix(cmd.getArgumentName()).withCondition(cmd::canUse);
      cmd.populateUsages(prefixed);
    }
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(literal("reload")
        .executes(c -> {
          plugin.reload();
          c.getSource().sendSuccess(Component.text("Reloaded Usables plugin"));
          return 0;
        })
    );

    command.then(literal("save")
        .executes(c -> {
          plugin.save();
          c.getSource().sendSuccess(Component.text("Saved Usables plugin"));
          return 0;
        })
    );

    for (var node: UsablesCommands.usableCommands) {
      var literal = literal(node.getArgumentName());
      literal.requires(node::canUse);
      node.create(literal);
      command.then(literal);
    }
  }
}
