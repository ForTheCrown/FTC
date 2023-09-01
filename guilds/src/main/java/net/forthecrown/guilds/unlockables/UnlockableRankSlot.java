package net.forthecrown.guilds.unlockables;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildRank.NOT_SET;
import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.menu.GuildRanksMenu;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.DefaultItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

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
    return rankNode(getId(), this);
  }

  public static MenuNode rankNode(int rankId,
                                  @Nullable UnlockableRankSlot unlockable
  ) {
    return MenuNode.builder()
        .setItem((user, context) -> {
          var guild = context.getOrThrow(GUILD);
          var rank = guild.getSettings().getRank(rankId);

          DefaultItemBuilder builder;

          if ((unlockable == null || unlockable.isUnlocked(guild))
              && rank != null
          ) {
            builder = ItemStacks.builder(Material.NAME_TAG)
                .setName(rank.getName())
                .addLore(rank.getDescription());

            if (rankId != ID_LEADER) {
              builder
                  .addLoreRaw(empty())
                  .addLore("&7Shift-left-click to edit permissions")
                  .addLore("&7Shift-right-click to edit auto level-up value");
            }

            builder.addLore("&7Left-click to rename");

            if (rankId != ID_LEADER) {
              builder.addLore("&7Right-click to change max chunk claims")
                  .addLoreRaw(empty());
            }

            builder.addLore("&7Rank ID: " + rank.getId());

            if (rank.getTotalExpLevelUp() != NOT_SET) {
              builder.addLore(
                  Text.format(
                      "Level up Exp: {0, number}",
                      NamedTextColor.GRAY,
                      rank.getTotalExpLevelUp()
                  )
              );
            }

            if (rank.getMaxChunkClaims() != NOT_SET) {
              builder.addLore(
                  Text.format(
                      "Max chunk claims: {0, number}",
                      NamedTextColor.GRAY,
                      rank.getMaxChunkClaims()
                  )
              );
            }
          } else {
            assert unlockable != null : "Unlockable is null";

            builder = ItemStacks.builder(Material.GUNPOWDER)
                .setName("Rank Slot")

                .addLore("Not yet unlocked")
                .addLore(unlockable.getProgressComponent(guild))

                .addLore(empty())
                .addLore(unlockable.getClickComponent())
                .addLore(unlockable.getShiftClickComponent());
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          var guild = context.getOrThrow(GUILD);
          var rank = guild.getSettings().getRank(rankId);

          if (unlockable == null) {
            GuildRanksMenu.onDefaultRanksClick(user, click, rank, guild);
            return;
          }

          unlockable.onClick(user, click, context, () -> {
            GuildRanksMenu.onRankClick(user, guild, click, rank);
          });
        })

        .build();
  }
}