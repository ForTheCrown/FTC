package net.forthecrown.guilds.unlockables;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.menu.GuildRanksMenu;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.DefaultItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
@Getter
public enum UnlockableRankSlot implements Unlockable {
    RANK_1(29, 1, 1000),
    RANK_2(30, 2, 2000),
    RANK_3(31, 3, 5000),
    RANK_4(32, 4, 10000),
    RANK_5(33, 5, 25000),
    ;

    private final int slot, id, expRequired;

    @Override
    public GuildPermission getPerm() {
        return GuildPermission.CAN_CHANGE_RANKS;
    }

    @Override
    public String getKey() {
        return name().toLowerCase();
    }

    @Override
    public Component getName() {
        return text("Extra Rank Slot", NamedTextColor.YELLOW);
    }

    @Override
    public void onUnlock(Guild guild, User user) {
        guild.getSettings().addRank(getId());
    }

    @Override
    public MenuNode toInvOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var guild = context.getOrThrow(GUILD);
                    var rank = guild.getSettings().getRank(id);

                    DefaultItemBuilder builder;

                    if (isUnlocked(guild) && rank != null) {
                        builder = ItemStacks.builder(Material.NAME_TAG)
                                .setName(rank.getName())

                                .addLore(rank.getDescription())
                                .addLore("&7Click to rename")
                                .addLore("&7Shift-click to edit permissions")
                                .addLore("&7Rank ID: " + rank.getId());
                    } else {
                        builder = ItemStacks.builder(Material.GUNPOWDER)
                                .setName("Rank Slot")

                                .addLore("Not yet unlocked")
                                .addLore(getProgressComponent(guild))

                                .addLore(empty())
                                .addLore(getClickComponent())
                                .addLore(getShiftClickComponent());
                    }

                    return builder.build();
                })

                .setRunnable((user, context, click) -> {
                    onClick(user, click, context, () -> {
                        var guild = context.getOrThrow(GUILD);
                        var rank = guild.getSettings()
                                .getRank(getId());

                        GuildRanksMenu.onRankClick(user, guild, click, rank);
                    });
                })

                .build();
    }
}