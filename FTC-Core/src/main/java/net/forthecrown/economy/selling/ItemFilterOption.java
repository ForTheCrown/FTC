package net.forthecrown.economy.selling;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import org.bukkit.Material;

import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class ItemFilterOption implements CordedInventoryOption {
    public static ItemFilterOption
            NAME_OPTION = new ItemFilterOption(
                    new InventoryPos(3, 0),
                    filter -> filter.ignoreNamed,
                    (bool, filter) -> filter.ignoreNamed = bool,
                    "Ignoring named items",
                    "Accepting named items"
            ),

            LORE_OPTION = new ItemFilterOption(
                    new InventoryPos(5, 0),
                    filter -> filter.ignoreLore,
                    (bool, filter) -> filter.ignoreLore = bool,
                    "Ignoring items with lore",
                    "Accepting items with lore"
            );

    @Getter
    private final InventoryPos pos;
    private final Function<ItemFilter, Boolean> getter;
    private final BiConsumer<Boolean, ItemFilter> setter;
    private final String trueName, falseName;

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        boolean state = getter.apply(user.getSellShopFilter());

        ItemStackBuilder builder = new ItemStackBuilder(state ? Material.BLACK_STAINED_GLASS_PANE : Material.WHITE_STAINED_GLASS_PANE, 1)
                .setName(state ? trueName : falseName, true)
                .addLore("&7Click to switch", true);

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        boolean state = !getter.apply(user.getSellShopFilter());
        setter.accept(state, user.getSellShopFilter());

        context.setReloadInventory(true);
    }
}