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
            new ItemAttributeNode()
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
        protected void createCommand(BrigadierCommand command) {
            for (var node: NODES) {
                var literal = literal(node.getName());

                node.create(literal);
                node.register();

                command.then(literal);
            }
        }
    }

}