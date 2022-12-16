package net.forthecrown.guilds.menu;

import lombok.Getter;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class CosmeticsMenu extends MenuPage {
    private final GuildColorMenu primaryColor;
    private final GuildColorMenu secondaryColor;
    private final NameFormatMenu nameFormat;

    public static final int
            SLOT_PRIMARY_COLOR = 20,
            SLOT_SECONDARY_COLOR = 22,
            SLOT_NAME_FORMAT = 24;

    public CosmeticsMenu(MenuPage parent) {
        super(parent);

        primaryColor = new GuildColorMenu(this, "Primary Color");
        secondaryColor = new GuildColorMenu(this, "Secondary Color");
        nameFormat = new NameFormatMenu(this);

        initMenu(Menus.builder(45, "Guild cosmetics"), true);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        builder.add(SLOT_PRIMARY_COLOR, primaryColor);
        builder.add(SLOT_SECONDARY_COLOR, secondaryColor);
        builder.add(SLOT_NAME_FORMAT, nameFormat);
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return ItemStacks.builder(Material.LOOM)
                .setName(Component.text("Cosmetics", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false))
                .addLore(Component.text("Customization for the guild.", NamedTextColor.GRAY))
                .build();
    }

    @Override
    protected MenuNode createHeader() {
        return this;
    }
}