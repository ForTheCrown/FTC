package net.forthecrown.events.player;

import java.util.function.Function;
import net.forthecrown.core.AfkKicker;
import net.forthecrown.core.Messages;
import net.forthecrown.core.TabList;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.login.LoginEffect;
import net.forthecrown.cosmetics.login.LoginEffects;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    UserLookup lookup = UserManager.get().getUserLookup();
    UserLookupEntry entry = lookup.getEntry(player.getUniqueId());

    // If entry is null then it's a new player or a player whose
    // cache entry was deleted or doesn't exist for some reason
    if (entry == null) {
      entry = lookup.createEntry(player);
    }

    User user = Users.get(entry);
    boolean nameChanged = user.onJoin();
    TabList.update();

    PacketListeners.inject(player);
    AfkKicker.addOrDelay(user.getUniqueId());

    Users.updateVanished();

    if (!player.hasPlayedBefore()) {
      player.teleport(GeneralConfig.getServerSpawn());
      event.joinMessage(Messages.firstJoin(user));
      user.setTimeToNow(TimeField.FIRST_JOIN);

      // Give royal sword
      ItemStack sword = ExtendedItems.ROYAL_SWORD.createItem(user.getUniqueId());
      user.getInventory().addItem(sword);

      // Give join kit
      Kit kit = Usables.getInstance()
          .getKits()
          .get(GeneralConfig.onFirstJoinKit);

      if (kit != null) {
        kit.interact(user.getPlayer());
      }

      // Display some info
      Tasks.runLaterAsync(() -> user.sendMessage(Messages.RANK_CHAT_INFO), 200);

      return;
    }

    event.joinMessage(null);
    user.sendMessage(Messages.WELCOME_BACK);

    if (user.get(Properties.VANISHED)) {
      return;
    }

    if (nameChanged) {
      String lastName = user.getPreviousNames()
          .get(user.getPreviousNames().size() - 1);

      sendLogMessage(audience -> {
        Component name = LoginEffects.getDisplayName(user, audience);
        return Messages.newNameJoinMessage(name, lastName);
      });
    } else {
      sendLoginMessage(user);
    }
  }

  public static void sendLoginMessage(User user) {
    sendLogMessage(audience -> {
      Component name = LoginEffects.getDisplayName(user, audience);
      LoginEffect effect = user.getCosmeticData().get(Cosmetics.LOGIN);
      return Messages.joinMessage(name, effect);
    });
  }

  public static void sendLogoutMessage(User user) {
    sendLogMessage(audience -> {
      Component name = LoginEffects.getDisplayName(user, audience);
      LoginEffect effect = user.getCosmeticData().get(Cosmetics.LOGIN);
      return Messages.leaveMessage(name, effect);
    });
  }

  static void sendLogMessage(Function<Audience, Component> renderer) {
    for (var a : Bukkit.getServer().audiences()) {
      var text = renderer.apply(a);

      if (text != null) {
        a.sendMessage(text);
      }
    }
  }
}