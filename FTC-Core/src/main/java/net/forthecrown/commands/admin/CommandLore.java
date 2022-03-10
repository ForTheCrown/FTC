package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CommandLore extends FtcCommand {
    public CommandLore() {
        super("lore", Crown.inst());

        setAliases("itemlore");
        setPermission(Permissions.ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("set")
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .then(argument("lore", ChatArgument.chat())
                                        .executes(c -> set(c,
                                                c.getArgument("index", Integer.class),
                                                c.getArgument("lore", Component.class)
                                        ))
                                )
                        )
                )

                .then(literal("add")
                        .then(argument("lore", ChatArgument.chat())
                                .executes(c -> add(c, c.getArgument("lore", Component.class)))
                        )
                )

                .then(literal("clear")
                        .executes(c -> {
                            ItemStack item = c.getSource().asPlayer().getInventory().getItemInMainHand();
                            checkItem(item);

                            ItemMeta meta = item.getItemMeta();
                            meta.lore(new ArrayList<>());
                            item.setItemMeta(meta);

                            c.getSource().sendAdmin("Cleared item lore");
                            return 0;
                        })
                );
    }

    private int set(CommandContext<CommandSource> c, int index, Component lore) throws CommandSyntaxException {
        ItemStack item = c.getSource().asPlayer().getInventory().getItemInMainHand();
        checkItem(item);

        ItemMeta meta = item.getItemMeta();

        List<Component> lores = meta.hasLore() ? meta.lore() : new ArrayList<>();
        try {
            lores.set(index, lore);
        } catch (IndexOutOfBoundsException e){
            throw FtcExceptionProvider.create("Index out of bounds: " + e.getMessage());
        }

        meta.lore(lores);
        item.setItemMeta(meta);

        c.getSource().sendAdmin(
                Component.text("Set " + index + " to ")
                        .append(lore)
        );
        return 0;
    }

    private int add(CommandContext<CommandSource> c, Component lore) throws CommandSyntaxException {
        ItemStack item = c.getSource().asPlayer().getInventory().getItemInMainHand();
        checkItem(item);

        ItemMeta meta = item.getItemMeta();

        List<Component> lores = meta.hasLore() ? meta.lore() : new ArrayList<>();
        lores.add(lore);

        meta.lore(lores);
        item.setItemMeta(meta);

        c.getSource().sendAdmin(
                Component.text("Added ")
                        .append(lore)
                        .append(Component.text(" to lores"))
        );
        return 0;
    }

    public static void checkItem(ItemStack itemStack) throws CommandSyntaxException {
        if(ItemStacks.isEmpty(itemStack)) throw FtcExceptionProvider.mustHoldItem();
    }

    private static LiteralArgumentBuilder<CommandSource> arg(String name){
        return LiteralArgumentBuilder.literal(name);
    }

    private static <T> RequiredArgumentBuilder<CommandSource, T> arg(String name, ArgumentType<T> type){
        return RequiredArgumentBuilder.argument(name, type);
    }

    public interface ComponentCommand {
        int run(CommandContext<CommandSource> context, Component lore) throws CommandSyntaxException;
    }
}
