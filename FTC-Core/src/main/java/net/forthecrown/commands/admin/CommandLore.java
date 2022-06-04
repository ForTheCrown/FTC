package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ChatUtils;
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
                        .then(argument("index", IntegerArgumentType.integer(1))
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

                .then(literal("remove")
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(c -> remove(c, c.getArgument("index", Integer.class)))
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

    private int remove(CommandContext<CommandSource> c, int index) throws CommandSyntaxException {
        ItemStack item = c.getSource().asPlayer().getInventory().getItemInMainHand();
        checkItem(item);

        ItemMeta meta = item.getItemMeta();

        if (!meta.hasLore()) {
            throw FtcExceptionProvider.create("Item has no lore");
        }

        List<Component> lores = meta.hasLore() ? meta.lore() : new ArrayList<>();

        if (index > lores.size()) {
            throw FtcExceptionProvider.create("Index out of bounds: " + index + ", too big for size: " + lores.size());
        }

        Component text = lores.remove(index - 1);
        meta.lore(lores);
        item.setItemMeta(meta);

        c.getSource().sendAdmin(
                Component.text("Removed lore at index " + index + ": ")
                        .append(text)
        );
        return 0;
    }

    private int set(CommandContext<CommandSource> c, int index, Component lore) throws CommandSyntaxException {
        ItemStack item = c.getSource().asPlayer().getInventory().getItemInMainHand();
        checkItem(item);

        ItemMeta meta = item.getItemMeta();

        if (!meta.hasLore()) {
            throw FtcExceptionProvider.create("Item has no lore");
        }

        List<Component> lores = meta.hasLore() ? meta.lore() : new ArrayList<>();

        if (index > lores.size()) {
            throw FtcExceptionProvider.create("Index out of bounds: " + index + ", too big for size: " + lores.size());
        }

        lores.set(index - 1, ChatUtils.wrapForItems(lore));

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
        lores.add(ChatUtils.wrapForItems(lore));

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

    public interface ComponentCommand {
        int run(CommandContext<CommandSource> context, Component lore) throws CommandSyntaxException;
    }
}