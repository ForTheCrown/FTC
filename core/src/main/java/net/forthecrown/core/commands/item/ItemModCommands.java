package net.forthecrown.core.commands.item;

import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import org.bukkit.permissions.Permission;

public class ItemModCommands {

  static final Permission PERMISSION = Permissions.registerCmd("items");

  private static final ItemModifierNode[] NODES = {
      new EnchantmentNode(),
      new ItemLoreNode(),
      new ItemNameNode(),
      new ItemDataNode(),
      new ItemAttributeNode(),
      new ItemCooldownNode()
  };

  public static void createCommands() {
    new ItemStacksCommand().register();
  }

  public static class ItemStacksCommand extends FtcCommand {

    public ItemStacksCommand() {
      super("items");

      setAliases("itemstacks", "itemstack", "item");
      setPermission(PERMISSION);
    }

    @Override
    public void populateUsages(UsageFactory factory) {
      for (var n: NODES) {
        var prefixed = factory.withPrefix(n.getArgumentName());
        n.populateUsages(prefixed);
      }
    }

    @Override
    public void createCommand(GrenadierCommand command) {
      for (var node : NODES) {
        var literal = literal(node.getArgumentName());

        node.create(literal);
        node.register();

        command.then(literal);
      }
    }
  }

}