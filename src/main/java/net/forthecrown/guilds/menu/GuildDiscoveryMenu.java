package net.forthecrown.guilds.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.guilds.DiscoverySort;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.forthecrown.guilds.menu.GuildMenus.DISC_PAGE;
import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.forthecrown.utils.inventory.menu.Menus.MAX_INV_SIZE;

public class GuildDiscoveryMenu extends ListPage<Guild> {
    private final StatisticsMenu statisticsMenu;

    public GuildDiscoveryMenu() {
        super(null, DISC_PAGE);

        this.statisticsMenu = new StatisticsMenu(this);

        initMenu(
                Menus.builder(MAX_INV_SIZE, "Guild Discovery"),
                true
        );
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        super.createMenu(builder);

         for (var s: DiscoverySort.values()) {
             builder.add(s.getSlot(), s.toInvOption());
         }
    }

    @Override
    protected List<Guild> getList(User user, InventoryContext context) {
        List<Guild> list = GuildManager.get().getGuilds();

        boolean invert = user.get(Properties.G_DISC_SORT_INVERTED);
        DiscoverySort sort = user.get(Properties.DISCOVERY_SORT);
        list.sort(invert ? sort.reversed() : sort);

        return list;
    }

    @Override
    protected ItemStack getItem(User user,
                                Guild entry,
                                InventoryContext context
    ) {
        var builder = ItemStacks.toBuilder(
                entry.getSettings().getBanner().clone()
        );

        builder.clearLore()
                .clearEnchants()
                .setFlags(ItemFlag.HIDE_POTION_EFFECTS);

        builder.setName(entry.displayName());

        if (Objects.equals(entry, user.getGuild())) {
            builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                    .addFlags(ItemFlag.HIDE_ENCHANTS)
                    .addLore("Your guild!");
        }

        var leader = entry.getLeader().getUser();

        var writer = TextWriters.loreWriter();
        writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
        writer.setFieldValueStyle(Style.style(NamedTextColor.WHITE));

        writer.field("Leader", leader.displayName());
        writer.field("Total Exp", Text.formatNumber(entry.getTotalExp()));
        writer.field("Created", Text.formatDate(entry.getCreationTimeStamp()));
        writer.field("Members", Text.formatNumber(entry.getMemberSize()));

        builder.addLore(writer.getLore());
        builder.addLore("")
                .addLore("&7Click to view more info");

        return builder.build();
    }

    @Override
    protected void onClick(User user,
                           Guild entry,
                           InventoryContext context,
                           ClickContext click
    ) throws CommandSyntaxException {
        context.set(GUILD, entry);
        statisticsMenu.onClick(user, context, click);
    }

    @Override
    protected MenuNode createHeader() {
        return this;
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user,
                                          @NotNull InventoryContext context
    ) {
        return ItemStacks.builder(Material.COMPASS)
                .setName("&eGuild Discovery Menu")
                .build();
    }
}