package net.forthecrown.core.admin.ui;

import com.google.common.collect.Lists;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.MenuNodeItem;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static net.forthecrown.core.admin.ui.AdminUI.ENTRY;
import static net.forthecrown.core.admin.ui.AdminUI.PUNISHMENT;
import static net.forthecrown.utils.inventory.menu.Menus.DEFAULT_INV_SIZE;

class PunishPage extends AdminUiPage {

    public PunishPage(AdminUiPage parent) {
        super(Component.text("Punish user"), DEFAULT_INV_SIZE, parent);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        TimeSelectionPage timeSelection = new TimeSelectionPage(this);
        JailSelectorPage jailSelector = new JailSelectorPage(timeSelection, this);

        item = (user, context) -> {
            return ItemStacks.builder(Material.IRON_AXE)
                    .setName("Punish user")
                    .build();
        };

        Slot start = Slot.of(2, 1);

        for (var type: PunishType.TYPES) {
            builder.add(start.add(type.ordinal(), 0),
                    MenuNode.builder()
                            .setItem((user, context) -> {
                                var item = ItemStacks.builder(PunishmentListPage.typeToMaterial(type))
                                        .setName(type.presentableName());

                                var target = context.get(ENTRY);

                                if (target.isPunished(type)) {
                                    item
                                            .addEnchant(Enchantment.BINDING_CURSE, 1)
                                            .setFlags(ItemFlag.HIDE_ENCHANTS)
                                            .addLore("&cUser has already been punished with this");
                                }

                                var targetUser = target.getUser();
                                targetUser.unloadIfOffline();

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
                                    throw Exceptions.alreadyPunished(target, type);
                                }

                                if (!Punishments.canPunish(
                                        user.getCommandSource(null),
                                        target
                                )) {
                                    throw Exceptions.cannotPunish(target);
                                }

                                if (!user.hasPermission(type.getPermission())) {
                                    throw Exceptions.NO_PERMISSION;
                                }

                                context.set(PUNISHMENT, new PunishBuilder(entry, type));

                                if (type == PunishType.JAIL) {
                                    jailSelector.onClick(user, context, click);
                                } else if(type == PunishType.KICK) {
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

    static class JailSelectorPage extends ListUiPage<JailCell> {
        private final TimeSelectionPage timeSelection;

        public JailSelectorPage(TimeSelectionPage timeSelection, AdminUiPage parent) {
            super(Component.text("Which jail?"), parent);

            this.timeSelection = timeSelection;
        }

        @Override
        protected List<JailCell> getList(PunishEntry entry) {
            return Lists.newArrayList(Registries.JAILS);
        }

        @Override
        protected ItemStack getItem(JailCell entry, PunishEntry punishEntry) {
            var builder = ItemStacks.builder(Material.IRON_BARS)
                    .setName(
                            Registries.JAILS.getKey(entry)
                                    .orElse("UNKNOWN")
                    );

            var loreWriter = TextWriters.loreWriter();
            entry.writeDisplay(loreWriter);

            builder.setLore(loreWriter.getLore());

            return builder.build();
        }

        @Override
        protected void onClick(JailCell entry, int index, User user, InventoryContext context) {
            context.get(PUNISHMENT).setExtra(
                    Registries.JAILS.getKey(entry)
                            .orElse("UNKNOWN")
            );

            timeSelection.getInventory().open(user, context);
        }

        @Override
        protected MenuNodeItem createMenuButton() {
            return null;
        }
    }
}