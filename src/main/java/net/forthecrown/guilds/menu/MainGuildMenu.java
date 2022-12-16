package net.forthecrown.guilds.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.challenge.ChallengeBook;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

@Getter
public class MainGuildMenu extends MenuPage {
    private final StatisticsMenu stats;
    private final MessageBoardMenu messageBoard;
    private final UpgradesMenu upgradesMenu;

    public MainGuildMenu() {
        super(null);

        stats = new StatisticsMenu(this);
        messageBoard = new MessageBoardMenu(this);
        upgradesMenu = new UpgradesMenu(this);

        initMenu(
                Menus.builder(Menus.MAX_INV_SIZE - 9, "Guild Menu"),
                false
        );
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        builder.add(Slot.of(2, 2), stats);
        builder.add(Slot.of(3, 2), messageBoard);
        builder.add(Slot.of(5, 2), upgradesMenu);

        builder.add(Slot.of(4, 2),
                MenuNode.builder()
                        .setItem((user, context) -> {
                            return ItemStacks.builder(Material.KNOWLEDGE_BOOK)
                                    .setName("&eChallenges")
                                    .addLore("&7Challenges to gain guild Exp.")
                                    .build();
                        })

                        .setRunnable((user, context) -> {
                            context.shouldReloadMenu(false);
                            ChallengeBook.open(user);
                        })

                        .build()
        );

        builder.add(Slot.of(6, 2),
                MenuNode.builder()
                        .setItem((user, context) -> {
                            return ItemStacks.builder(Material.CHEST)
                                    .setName(Component.text("Guild Chest", NamedTextColor.YELLOW)
                                            .decoration(TextDecoration.ITALIC, false))
                                    .addLore(Component.text("A shared inventory for all members.", NamedTextColor.GRAY))
                                    .build();
                        })

                        .setRunnable((user, context, click) -> {
                            var guild = context.getOrThrow(GUILD);
                            user.getPlayer().openInventory(guild.getInventory());
                        })

                        .build()
        );
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        var guild = context.getOrThrow(GUILD);
        var formattedBanner = guild.getSettings()
                .getBanner()
                .clone();

        ItemMeta meta = formattedBanner.getItemMeta();
        meta.displayName(Component.text("Guild Menu of ")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(guild.getName())
                        .color(guild.getSettings().getPrimaryColor().getTextColor())
                )
        );

        meta.lore(ObjectList.of(
                Component.text("Click on items to navigate the menu.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); // Hide applied banner patterns
        formattedBanner.setItemMeta(meta);

        return formattedBanner;
    }

    @Override
    public void onClick(User user, InventoryContext context, ClickContext click)
            throws CommandSyntaxException
    {
        var guild = context.getOrThrow(GUILD);

        if (!Objects.equals(user.getGuild(), guild)) {
            throw Exceptions.NOT_IN_GUILD;
        }

        super.onClick(user, context, click);
    }

    @Override
    protected MenuNode createHeader() {
        return this;
    }
}