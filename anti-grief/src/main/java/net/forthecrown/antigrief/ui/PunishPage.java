package net.forthecrown.antigrief.ui;

import static net.forthecrown.antigrief.ui.AdminUi.ENTRY;
import static net.forthecrown.antigrief.ui.AdminUi.HEADER;
import static net.forthecrown.antigrief.ui.AdminUi.PAGE;
import static net.forthecrown.antigrief.ui.AdminUi.PUNISHMENT;
import static net.forthecrown.menu.Menus.DEFAULT_INV_SIZE;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.antigrief.JailCell;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.antigrief.GExceptions;
import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.ListPage;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PunishPage extends MenuPage {

  public PunishPage(MenuPage parent) {
    super(parent);
    initMenu(
        Menus.builder(DEFAULT_INV_SIZE, Component.text("Punish user")),
        true
    );
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.IRON_AXE)
        .setName("Punish user")
        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return HEADER;
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    TimeSelectionPage timeSelection = new TimeSelectionPage(this);
    JailSelectorPage jailSelector = new JailSelectorPage(timeSelection, this);

    Slot start = Slot.of(2, 1);

    for (var type : PunishType.TYPES) {
      builder.add(start.add(type.ordinal(), 0),
          MenuNode.builder()
              .setItem((user, context) -> {
                var item = ItemStacks.builder(PunishmentListPage.typeToMaterial(type))
                    .setName(type.presentableName());

                var target = context.getOrThrow(ENTRY);

                if (target.isPunished(type)) {
                  item
                      .addEnchant(Enchantment.BINDING_CURSE, 1)
                      .setFlags(ItemFlag.HIDE_ENCHANTS)
                      .addLore("&cUser has already been punished with this");
                }

                var targetUser = target.getUser();

                if (type == PunishType.KICK && !targetUser.isOnline()) {
                  item
                      .addEnchant(Enchantment.BINDING_CURSE, 1)
                      .setFlags(ItemFlag.HIDE_ENCHANTS)
                      .addLore("&7User is not online, cannot be kicked");
                }

                if (!user.hasPermission(type.getPermission())) {
                  item.addLore("&cCannot punish! No permission to give this type of punishment");
                }

                return item.build();
              })

              .setRunnable((user, context, click) -> {
                var entry = context.get(ENTRY);
                var target = entry.getUser();

                if (entry.isPunished(type)) {
                  throw GExceptions.alreadyPunished(target, type);
                }

                if (!Punishments.canPunish(
                    user.getCommandSource(),
                    target
                )) {
                  throw GExceptions.cannotPunish(target);
                }

                if (!user.hasPermission(type.getPermission())) {
                  throw Exceptions.NO_PERMISSION;
                }

                context.set(PUNISHMENT, new PunishBuilder(entry, type));

                if (type == PunishType.JAIL) {
                  jailSelector.onClick(user, context, click);
                } else if (type == PunishType.KICK) {
                  if (target.isOnline()) {
                    target.getPlayer().kick(null, PlayerKickEvent.Cause.KICK_COMMAND);
                    click.shouldReloadMenu(true);
                  }
                } else {
                  timeSelection.onClick(user, context, click);
                }
              })

              .build()
      );
    }
  }

  static class JailSelectorPage extends ListPage<JailCell> {

    private final TimeSelectionPage timeSelection;

    public JailSelectorPage(TimeSelectionPage timeSelection, MenuPage parent) {
      super(parent, PAGE);

      this.timeSelection = timeSelection;

      initMenu(
          Menus.builder(Component.text("Which jail?")),
          true
      );
    }

    @Override
    protected List<JailCell> getList(User user, Context context) {
      return Lists.newArrayList(Punishments.get().getCells());
    }

    @Override
    protected ItemStack getItem(User user, JailCell entry, Context context) {
      var builder = ItemStacks.builder(Material.IRON_BARS)
          .setName(
              Punishments.get().getCells()
                  .getKey(entry)
                  .orElse("UNKNOWN")
          );

      var loreWriter = TextWriters.buffered();
      entry.writeDisplay(loreWriter);

      builder.setLore(loreWriter.getLore());

      return builder.build();
    }

    @Override
    protected void onClick(User user, JailCell entry, Context context, ClickContext click)
        throws CommandSyntaxException
    {
      context.get(PUNISHMENT).setExtra(
          Punishments.get().getCells()
              .getKey(entry)
              .orElse("UNKNOWN")
      );

      timeSelection.getMenu().open(user, context);
    }

    @Override
    protected MenuNode createHeader() {
      return HEADER;
    }
  }
}