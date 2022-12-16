package net.forthecrown.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.Text;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;

import static net.forthecrown.commands.CommandNickname.CLEAR;

public class ItemNameNode extends ItemModifierNode {
    public ItemNameNode() {
        super("itemname", "nameitem", "renameitem", "itemrename");
    }

    @Override
    public void create(LiteralArgumentBuilder<CommandSource> command) {
        command
                .then(argument("name", Arguments.CHAT)
                        .suggests((context, builder) -> {
                            var token = builder.getRemainingLowerCase();

                            if (token.isBlank() || CLEAR.startsWith(token)) {
                                builder.suggest(CLEAR);
                                return builder.buildFuture();
                            }

                            return Arguments.CHAT.listSuggestions(context, builder);
                        })

                        .executes(c -> {
                            var held = getHeld(c.getSource());
                            var meta = held.getItemMeta();
                            var name = c.getArgument("name", Component.class);

                            if (Text.isDashClear(name)) {
                                meta.displayName(null);
                                c.getSource().sendAdmin(Messages.CLEARED_ITEM_NAME);
                            } else {
                                meta.displayName(Text.wrapForItems(name));
                                c.getSource().sendAdmin(Messages.setItemName(name));
                            }

                            held.setItemMeta(meta);
                            return 0;
                        })
                );
    }
}