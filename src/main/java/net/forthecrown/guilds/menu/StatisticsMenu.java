package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.forthecrown.commands.guild.GuildInviteNode;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class StatisticsMenu extends MenuPage {

  private final MembersMenu members;

  public static final int
      SLOT_GENERAL = 20,
      SLOT_MEMBERS = 21,
      SLOT_EXP = 22,
      SLOT_EFFECTS = 23,
      SLOT_BANNER = 24;

  private static final Slot
      SLOT_JOIN = Slot.of(4, 4);

  public StatisticsMenu(MenuPage parent) {
    super(parent);

    members = new MembersMenu(this);

    initMenu(Menus.builder(45, "Guild statistics"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(SLOT_MEMBERS, members);

    // Join button that's only available when the guild is public and
    // when the viewer doesn't already have a guild
    builder.add(SLOT_JOIN,
        MenuNode.builder()
            .setItem((user, context) -> {
              var guild = context.getOrThrow(GUILD);

              if (!guild.getSettings().isPublic()
                  || user.getGuild() != null
              ) {
                return null;
              }

              var i = ItemStacks.builder(Material.WRITABLE_BOOK)
                  .setName(
                      Text.format(
                          "Click to join {0}",
                          guild.displayName()
                      )
                  );

              if (guild.isFull()) {
                i.addLore("This guild is full and cannot be joined :(")
                    .addEnchant(Enchantment.BINDING_CURSE, 1)
                    .addFlags(ItemFlag.HIDE_ENCHANTS);
              } else {
                i.addLore(
                    "This guild is public and can be " +
                        "joined without invitation"
                );
              }

              return i.build();
            })

            .setRunnable((user, context, click) -> {
              var guild = context.getOrThrow(GUILD);

              if (!guild.getSettings().isPublic()
                  || user.getGuild() != null
              ) {
                return;
              }

              GuildInviteNode.ensureJoinable(user, guild);
              guild.join(user);
              click.shouldReloadMenu(true);
            })

            .build()
    );

    builder.add(SLOT_BANNER,
        MenuNode.builder()
            .setItem((user, context) -> {
              var item = context.getOrThrow(GUILD)
                  .getSettings()
                  .getBanner()
                  .clone();

              ItemMeta meta = item.getItemMeta();
              meta.displayName(Component.text("Guild Banner", NamedTextColor.YELLOW)
                  .decoration(TextDecoration.ITALIC, false));
              item.setItemMeta(meta);

              return item;
            })

            .build()
    );

    builder.add(SLOT_GENERAL,
        MenuNode.builder()
            .setItem((user, context) -> {
              var guild = context.getOrThrow(GUILD);

              return ItemStacks.builder(Material.GOLDEN_HELMET)
                  .setName("&eGeneral")
                  .setLore(
                      List.of(
                          Component.text("Guild Leader: ", NamedTextColor.GOLD)
                              .append(guild.getLeader().getUser().displayName()
                                  .color(NamedTextColor.WHITE)),
                          Component.text("Guild created on: ", NamedTextColor.GOLD)
                              .append(guild.getFormattedCreationDate()))
                  )

                  .addLore(
                      Component.text(guild.getSettings().allowsVisit() ?
                              "Has a public /visit location." :
                              "Does not have a public /visit location.")
                          .color(NamedTextColor.WHITE)
                          .decoration(TextDecoration.ITALIC, false)
                  )

                  .build();
            })

            .build()
    );

    builder.add(SLOT_EXP,
        MenuNode.builder()
            .setItem((user, context) -> {
              var guild = context.getOrThrow(GUILD);

              return ItemStacks.builder(Material.EXPERIENCE_BOTTLE)
                  .setName("&eGuild Experience")
                  .setFlags(ItemFlag.HIDE_ATTRIBUTES)

                  // Garbled mess, use Text#format()
                  .addLoreRaw(Component.text("Total EXP earned: ", NamedTextColor.GOLD)
                      .decoration(TextDecoration.ITALIC, false)
                      .append(Component.text(guild.getTotalExp())
                          .color(NamedTextColor.WHITE))
                  )
                  .addLoreRaw(Component.text("EXP earned today: ", NamedTextColor.GOLD)
                      .decoration(TextDecoration.ITALIC, false)
                      .append(Component.text(guild.getTotalTodayExp())
                          .color(NamedTextColor.WHITE))
                  )
                  .addLoreRaw(Component.text("Top EXP contributor: ", NamedTextColor.GOLD)
                      .decoration(TextDecoration.ITALIC, false)
                      .append(guild.getTopContributor().displayName())
                      .color(NamedTextColor.WHITE)
                  )

                  .build();
            })

            .build()
    );

    builder.add(SLOT_EFFECTS,
        MenuNode.builder()
            .setItem((user, context) -> {
              var guild = context.getOrThrow(GUILD);

              var item = ItemStacks.builder(Material.GLASS)
                  .setName("&eGuild Chunks")
                  .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                  .build();

              ItemMeta meta = item.getItemMeta();
              List<Component> lore = new ArrayList<>();

              lore.add(Component.text(
                      guild.activeEffectCount() > 0 ? "Active Effects: " : "No active effects.")
                  .color(NamedTextColor.GOLD)
                  .decoration(TextDecoration.ITALIC, false));

              guild.getActiveEffects().forEach(e -> {
                lore.add(Component.text("- ", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(e.getName().color(NamedTextColor.WHITE)));
              });

              meta.lore(lore);
              item.setItemMeta(meta);
              return item;
            })

            .build()
    );
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
    return ItemStacks.builder(Material.NAME_TAG)
        .setName(Component.text("Statistics", NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false))
        .addLore(Component.text("Information about the guild.", NamedTextColor.GRAY))
        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }
}