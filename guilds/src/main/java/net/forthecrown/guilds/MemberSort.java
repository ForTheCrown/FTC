package net.forthecrown.guilds;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import java.util.Comparator;
import lombok.Getter;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

@Getter
public enum MemberSort {

  // Leader first
  BY_RANK(
      "Guild Rank",
      17,
      Comparator.comparing(GuildMember::getRankId).reversed()
  ),

  // Oldest first
  BY_JOIN_DATE(
      "Join Date",
      26,
      Comparator.comparing(GuildMember::getJoinDate)
  ),

  // Most first
  BY_TOTAL_EXP(
      "Total Exp Earned",
      35,
      Comparator.comparing(GuildMember::getTotalExpEarned).reversed()
  ),

  // Most first
  BY_TODAY_EXP(
      "Exp Earned Today",
      44,
      Comparator.comparing(GuildMember::getExpEarnedToday).reversed()
  ),
  ;

  private final String text;
  private final int slot;
  private final Comparator<GuildMember> comparator;
  private final MenuNode node;

  MemberSort(String text, int slot, Comparator<GuildMember> comparator) {
    this.text = text;
    this.slot = slot;
    this.comparator = comparator;

    this.node = MenuNode.builder()
        .setItem((user, context) -> {
          var guild = context.getOrThrow(GUILD);

          var builder = ItemStacks.builder(guild.getSettings().getPrimaryColor().toGlassPane())
              .setName(Component.text("Sort by " + text).decoration(TextDecoration.ITALIC, false)
                  .color(NamedTextColor.AQUA))
              .addLore(
                  Component.text("Set the order in which").decoration(TextDecoration.ITALIC, false)
                      .color(NamedTextColor.GRAY))
              .addLore(Component.text("the members are displayed.")
                  .decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY))
              .setFlags(ItemFlag.HIDE_ENCHANTS);

          if (user.get(GUserProperties.MEMBER_SORT) == this) {
            builder.addEnchant(Enchantment.CHANNELING, 1);
          }

          return builder.build();
        })

        .setRunnable((user, context) -> {
          user.set(GUserProperties.MEMBER_SORT, this);
          context.shouldReloadMenu(true);
        })

        .build();
  }
}