package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandItemName extends CrownCommandBuilder {
    public CommandItemName(){
        super("itemname", FtcCore.getInstance());

        setAliases("itemrename", "nameitem", "renameitem");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("name", StringArgumentType.greedyString())
                        .executes(c -> rename(c.getSource(), ComponentUtils.convertString(c.getArgument("name", String.class))))
                )
                .then(argument("-component")
                        .then(argument("cName", ComponentArgument.component())
                                .executes(c -> rename(c.getSource(), c.getArgument("cName", Component.class)))
                        )
                );
    }

    private int rename(CommandSource source, Component name) throws CommandSyntaxException {
        ItemStack item = source.asPlayer().getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR) throw FtcExceptionProvider.create("You need to hold an item in your main hand!");

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
