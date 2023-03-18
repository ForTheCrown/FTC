package net.forthecrown.commands.usables;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;

public class InteractableCommands {

  static final InteractableNode<?>[] NODES = {
      new TriggerNode(),
      new UsableBlockNode(),
      new UsableEntityNode()
  };

  public static void createCommands() {
    new CommandInteractable().register();
  }

  static class CommandInteractable extends FtcCommand {

    public CommandInteractable() {
      super("interactable");

      setPermission(Permissions.ADMIN);
      setAliases("usable");
    }

    @Override
    public void populateUsages(UsageFactory factory) {
      for (var n: NODES) {
        var prefixed = factory.withPrefix(n.argumentName);
        n.populateUsages(prefixed);
      }
    }

    @Override
    public void createCommand(GrenadierCommand command) {
      for (var n : NODES) {
        n.register();

        var literal = literal(n.argumentName);
        n.create(literal);

        command.then(literal);
      }
    }
  }

}