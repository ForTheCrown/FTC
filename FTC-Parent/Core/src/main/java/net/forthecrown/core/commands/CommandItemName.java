package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandItemName extends FtcCommand {
    public CommandItemName(){
        super("itemname", CrownCore.inst());

        setAliases("itemrename", "nameitem", "renameitem");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("name", StringArgumentType.greedyString())
                        .executes(c -> rename(c.getSource(), ChatUtils.convertString(c.getArgument("name", String.class))))
                )
                .then(literal("-component")
                        .then(argument("cName", ComponentArgument.component())
                                .executes(c -> rename(c.getSource(), c.getArgument("cName", Component.class)))
                        )
                );
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
