package net.forthecrown.commands.item;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.permissions.Permission;

public class ItemModCommands extends CmdUtil {

  static final Permission PERMISSION = Permissions.ADMIN;

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
    protected void createCommand(BrigadierCommand command) {
      for (var node : NODES) {
        var literal = literal(node.getArgumentName());

        node.create(literal);
        node.register();

        command.then(literal);
      }
    }
  }

}