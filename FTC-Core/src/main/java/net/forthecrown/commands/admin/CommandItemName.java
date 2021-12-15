package net.forthecrown.commands.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandItemName extends FtcCommand {
    public CommandItemName(){
        super("itemname", Crown.inst());

        setAliases("itemrename", "nameitem", "renameitem");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        CommandLore.addCompOrStringArg(command, null, (context, lore) -> rename(context.getSource(), lore));
    }

    private int rename(CommandSource source, Component name) throws CommandSyntaxException {
        ItemStack item = source.asPlayer().getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR) throw FtcExceptionProvider.mustHoldItem();

        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);

        item.setItemMeta(meta);

        source.sendAdmin(
                Component.text("Renamed item to: ")
                        .append(name)
        );
        return 0;
    }
}
