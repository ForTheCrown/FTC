package net.forthecrown.emperor.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.utils.ChatUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CommandLore extends CrownCommandBuilder {
    public CommandLore() {
        super("lore", CrownCore.inst());

        setAliases("itemlore");
        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("set")
                        .then(compOrStringArg(
                                argument("loreIndex", IntegerArgumentType.integer(0)),
                                (c, b) -> Suggestions.empty(),
                                (c, l) -> set(c, c.getArgument("loreIndex", Integer.class), l)))
                )

                .then(compOrStringArg(
                        argument("add"),
                        (c, b) -> Suggestions.empty(),
                        this::add
                ))

                .then(argument("clear")
                        .executes(c -> {
                            ItemStack item = c.getSource().asPlayer().getInventory().getItemInMainHand();
                            checkItem(item);

                            ItemMeta meta = item.getItemMeta();
                            meta.lore(new ArrayList<>());

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
        if(itemStack == null || itemStack.getType() == Material.AIR) throw FtcExceptionProvider.create("You must be holding an item");
    }

    public static <T extends ArgumentBuilder<CommandSource, T>> T compOrStringArg(T arg, SuggestionProvider<CommandSource> s, ComponentCommand runnable){
        return arg
                .then(arg("string", StringArgumentType.greedyString())
                        .suggests(s)

                        .executes(c -> runnable.run(c, ChatUtils.convertString(c.getArgument("string", String.class))))
                )

                .then(componentArg("cLore", c-> runnable.run(c, c.getArgument("cLore", Component.class))));
    }

    public static LiteralArgumentBuilder<CommandSource> componentArg(String argName, Command<CommandSource> cmd){
        return arg("-component")
                .then(arg(argName, ComponentArgument.component())
                        .executes(cmd)
                );
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
