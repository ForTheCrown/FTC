package net.forthecrown.economy.selling;

import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.inventory.builder.options.SimpleOption;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public class PreviousPageOption extends SimpleOption {
    public static final PreviousPageOption OPTION = new PreviousPageOption();

    private PreviousPageOption() {
        super(0,
                new ItemStackBuilder(Material.PAPER, 1)
                        .setName(Component.text("< Previous Page").style(ChatFormatter.nonItalic(NamedTextColor.YELLOW)))
                        .build(),
                (user, context) -> SellShops.MAIN.open(user)
        );
    }
}
