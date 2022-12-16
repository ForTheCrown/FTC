package net.forthecrown.events.player;

import net.forthecrown.core.AfkKicker;
import net.forthecrown.core.Messages;
import net.forthecrown.core.TabList;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.cosmetics.login.LoginEffects;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class PlayerJoinListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            UserManager.get()
                    .getUserLookup()
                    .createEntry(player);
        }

        User user = Users.get(player);
        boolean nameChanged = user.onJoin();
        TabList.update();

        PacketListeners.inject(player);
        AfkKicker.addOrDelay(user.getUniqueId());

        if (!player.hasPlayedBefore()) {
            player.teleport(GeneralConfig.getServerSpawn());
            event.joinMessage(Messages.firstJoin(user));
            user.setTimeToNow(TimeField.FIRST_JOIN);

            // Give royal sword
            ItemStack sword = ExtendedItems.ROYAL_SWORD.createItem(user.getUniqueId());
            user.getInventory().addItem(sword);

            //Give join kit
            Kit kit = Usables.getInstance()
                    .getKits()
                    .get(GeneralConfig.onFirstJoinKit);

            if (kit != null) {
                kit.interact(user.getPlayer());
            }

            return;
        }

        event.joinMessage(null);
        user.sendMessage(Messages.WELCOME_BACK);

        if (!user.get(Properties.VANISHED)) {
            if (nameChanged) {
                String lastName = user.getPreviousNames()
                        .get(user.getPreviousNames().size() - 1);

                sendLogMessage(audience -> {
                    Component name = LoginEffects.createDisplayName(user, audience);
                    return Messages.newNameJoinMessage(name, lastName);
                });
            } else {
                sendLoginMessage(user);
            }
        }
    }

    public static void sendLoginMessage(User user) {
        sendLogMessage(audience -> {
            Component name = LoginEffects.createDisplayName(user, audience);
            return Messages.joinMessage(name);
        });
    }

    public static void sendLogoutMessage(User user) {
        sendLogMessage(audience -> {
            Component name = LoginEffects.createDisplayName(user, audience);
            return Messages.leaveMessage(name);
        });
    }

    static void sendLogMessage(Function<Audience, Component> renderer) {
        for (var a: Bukkit.getServer().audiences()) {
            var text = renderer.apply(a);

            if (text != null) {
                a.sendMessage(text);
            }
        }
    }
}