package net.forthecrown.commands.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
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
        command
                .then(literal("-clear")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            ItemStack held = user.getInventory().getItemInMainHand();

                            if (ItemStacks.isEmpty(held)) {
                                throw FtcExceptionProvider.mustHoldItem();
                            }

                            ItemMeta meta = held.getItemMeta();
                            meta.displayName(null);
                            held.setItemMeta(meta);

                            c.getSource().sendAdmin("Removed item name");
                            return 0;
                        })
                )

                .then(argument("name", ChatArgument.chat())
                        .suggests((context, builder) -> {
                            CommandSource source = context.getSource();

                            if(!source.isPlayer()) return Suggestions.empty();
                            Player player = source.asPlayer();
                            ItemStack held = player.getInventory().getItemInMainHand();

                            if(ItemStacks.isEmpty(held)) return Suggestions.empty();

                            Component display = FtcFormatter.itemDisplayName(held);

                            return CompletionProvider.suggestMatching(builder, ChatUtils.LEGACY.serialize(ChatUtils.renderToSimple(display)));
                        })

                        .executes(c -> rename(c.getSource(), c.getArgument("name", Component.class)))
                );
    }

    private int rename(CommandSource source, Component name) throws CommandSyntaxException {
        ItemStack item = source.asPlayer().getInventory().getItemInMainHand();
        if(ItemStacks.isEmpty(item)) throw FtcExceptionProvider.mustHoldItem();

        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.wrapForItems(name));

        item.setItemMeta(meta);

        source.sendAdmin(
                Component.text("Renamed item to: ")
                        .append(name)
        );
        return 0;
    }
}