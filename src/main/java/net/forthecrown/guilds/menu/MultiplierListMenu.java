package net.forthecrown.guilds.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.guilds.multiplier.DonatorMultiplier;
import net.forthecrown.guilds.multiplier.MultiplierType;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.ClickContext;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiplierListMenu extends ListPage<DonatorMultiplier> {
  private final MultiplierType type;

  public MultiplierListMenu(
      MenuPage parent,
      MultiplierType type
  ) {
    super(parent, GuildMenus.PAGE);
    this.type = type;

    initMenu(
        Menus.builder(
            Menus.sizeFromRows(5),
            type.getDisplayName() + " Multipliers"
        ),
        true
    );
  }

  @Override
  protected List<DonatorMultiplier> getList(User user, Context context) {
    return GuildManager.get()
        .getExpModifier()
        .getMultipliers(user.getUniqueId(), type);
  }

  @Override
  protected ItemStack getItem(User user,
                              DonatorMultiplier entry,
                              Context context
  ) {
    var builder = ItemStacks.builder(Material.PAPER)
        .setName("&eMultiplier " + entry.getModifier() + "x");

    builder.addLore(
        Text.format("Duration: {0, time, -short}",
            NamedTextColor.GRAY,
            entry.getDuration()
        )
    );

    var list = getList(user, context);
    list.removeIf(multiplier -> !multiplier.isActive());

    if (entry.isActive()) {
      builder.addLore("&7Already active")
          .addEnchant(Enchantment.BINDING_CURSE, 1)
          .setFlags(ItemFlag.HIDE_ENCHANTS)

          .addLore(
              Text.format("Activated: {0, date}",
                  NamedTextColor.GRAY,
                  entry.getActivationTime()
              )
          );

      long remaining = entry.getRemainingMillis();

      if (remaining > 0) {
        builder.addLore(
            Text.format("Remaining duration: {0, time, -short}",
                NamedTextColor.GRAY,
                remaining
            )
        );
      }
    } else if (user.getGuild() == null) {
     builder.addLore("&cCannot activate! Not in a guild");
    } else if (list.isEmpty()) {
      builder.addLore("&7Shift-Right-Click to activate");
    }

    return builder.build();
  }

  @Override
  protected void onClick(User user,
                         DonatorMultiplier entry,
                         Context context,
                         ClickContext click
  ) throws CommandSyntaxException {
    if (click.getClickType() != ClickType.SHIFT_RIGHT) {
      return;
    }

    if (user.getGuild() == null) {
      throw Exceptions.NOT_IN_GUILD;
    }

    if (entry.isActive()) {
      throw Exceptions.format("This is already active");
    }

    var list = getList(user, context);
    list.removeIf(multiplier -> !multiplier.isActive());

    if (list.size() > 0) {
      throw Exceptions.format(
          "You already have an active {0} multiplier",
          type.name().toLowerCase()
      );
    }

    GuildManager.get()
        .getExpModifier()
        .activate(entry);

    click.shouldReloadMenu(true);
  }

  @Override
  protected ItemStack createEmptyItem(User user, Context context) {
    return ItemStacks.builder(Material.SUGAR)
        .setName("&7You own no multipliers :(")
        .build();
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    var list = getList(user, context);
    int owned = list.size();

    list.removeIf(multiplier -> !multiplier.isActive());
    boolean hasActive = list.size() > 0;

    var builder = ItemStacks.builder(
        hasActive ? Material.FIRE_CHARGE : Material.FIREWORK_STAR
    );

    if (hasActive) {
      builder.addEnchant(Enchantment.BINDING_CURSE, 1)
          .setFlags(ItemFlag.HIDE_ENCHANTS);
    }

    builder.setName("&e" + type.getDisplayName() + " Multiplier")
        .addLore("&7Currently: " + (hasActive ? "&aActive" : "&6Inactive"))
        .addLore("&7Owned: " + owned)
        .addLore("");

    for (var s: type.getDescription()) {
      builder.addLore(s);
    }

    return builder.build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }

  @Override
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException {
    getMenu().open(user, context);
  }
}