package net.forthecrown.antigrief;

import static net.kyori.adventure.text.Component.text;

import java.util.List;
import java.util.Set;
import net.forthecrown.command.settings.BookSetting;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageRenderer;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class EavesDropper {
  private EavesDropper() {}

  /**
   * Determines if a user sees a muted user's chat messages with {@link EavesDropper}
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_MUTED
      = Properties.booleanProperty("eavesDrop_muted", false);

  /**
   * Determines if a user sees other people's direct messages through Eavesdropper
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_DM
      = Properties.booleanProperty("eavesDrop_dm", false);

  /**
   * Determines if a user sees what people write on signs with EavesDropper
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_SIGN
      = Properties.booleanProperty("eavesDrop_signs", false);

  /**
   * Determines if a user sees what other married users send to each other through marriage chat
   * with EavesDropper
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_MCHAT
      = Properties.booleanProperty("eavesDrop_mChat", false);

  /**
   * Determines if a user sees eaves dropper messages when other users mine into veins
   * <p>
   * Only affects uses with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_MINING
      = Properties.booleanProperty("eavesDrop_mining", false);

  /**
   * Determines if a user sees eaves dropper messages when guild members use their respective
   * guild's guild chat.
   */
  public static final UserProperty<Boolean> EAVES_DROP_GUILD_CHAT
      = Properties.booleanProperty("eavesDrop_guildChat", false);

  public static final Component PREFIX = text()
      .color(NamedTextColor.DARK_GRAY)
      .append(text("[", NamedTextColor.GRAY))
      .append(text("ED"))
      .append(text("] ", NamedTextColor.GRAY))
      .build();

  static void createSettings(SettingsBook<User> settingsBook) {
    Setting muted = Setting.create(EAVES_DROP_MUTED)
        .setDisplayName("Spy on muted")
        .setToggle("N{1} spying on muted users")
        .setToggleDescription("{Start} spying on muted players' messages")
        .setDescription("Toggles spying on people's muted messages");

    muted.createCommand(
        "eavesdrop_muted",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seemuted", "see_muted"
    );

    Setting dms = Setting.create(EAVES_DROP_DM)
        .setDisplayName("Spy on DMs")
        .setToggle("N{1} spying on people's DMs")
        .setToggleDescription("{Start} spying on players' DMs")
        .setDescription("Toggles spying on people's direct messages");

    dms.createCommand(
        "eavesdrop_dm",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seedms", "see_dms"
    );

    Setting signs = Setting.create(EAVES_DROP_SIGN)
        .setDisplayName("Spy on signs")
        .setToggle("N{1} spying on people's signs writings")
        .setToggleDescription("{Start} spying on what people write on signs")
        .setDescription("Toggles spying on what people write on signs");

    signs.createCommand(
        "eavesdrop_signs",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seesigns", "see_signs"
    );

    Setting mchat = Setting.create(EAVES_DROP_MCHAT)
        .setDisplayName("Spy on mchat")
        .setDescription("Toggles spying on people's marriage DMs")
        .setToggle("N{1} spying on marriage chat DMs")
        .setToggleDescription("{Start} spying on people's marriage DMs");

    mchat.createCommand(
        "eavesdrop_mchat",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seemchat", "seemarriage", "see_mchat", "see_marriage"
    );

    Setting gchat = Setting.create(EAVES_DROP_GUILD_CHAT)
        .setDisplayName("Spy on guilds")
        .setDescription("Toggles spying on guild chats")
        .setToggle("N{1} spying on guild chats")
        .setToggleDescription("{Start} spying on guild chats");

    gchat.createCommand(
        "eavesdrop_gchat",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seegchat", "see_gchat"
    );

    Setting veins = Setting.create(EAVES_DROP_MINING)
        .setDisplayName("Spy on miners")
        .setToggle("{0} mining notifications")
        .setEnableDescription("Get notified when people mine ores")
        .setDisableDescription("Don't get notified when people mine ores")
        .setDescription("Toggles being notified when players mine ores");

    veins.createCommand(
        "eavesdrop_veins",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN
    );

    List<BookSetting<User>> settings = settingsBook.getSettings();
    settings.add(muted.toBookSettng());
    settings.add(dms.toBookSettng());
    settings.add(signs.toBookSettng());
    settings.add(mchat.toBookSettng());
    settings.add(gchat.toBookSettng());
    settings.add(veins.toBookSettng());
  }

  public static void reportMessage(
      Audience source,
      Set<Audience> targets,
      UserProperty<Boolean> viewProperty,
      ViewerAwareMessage message,
      MessageRenderer renderer
  ) {
    ChannelledMessage ch = createChannelled(viewProperty, viewer -> {
      return renderer.render(viewer, message.create(viewer));
    });

    ch.setRenderer((viewer, baseMessage) -> {
      Mute mute = Punishments.muteStatus(source);
      Component muteText = text(mute.getPrefix());
      return Component.textOfChildren(PREFIX, muteText, baseMessage);
    });

    ch.filterTargets(audience -> {
      return !Audiences.equals(source, audience) && !Audiences.contains(audience, targets);
    });

    ch.send();
  }

  private static ChannelledMessage createChannelled(
      UserProperty<Boolean> viewProperty,
      ViewerAwareMessage message
  ) {
    ChannelledMessage ch = ChannelledMessage.create(message);
    ch.setChannelName("eaves_dropper");
    ch.addTargets(Bukkit.getOnlinePlayers());
    ch.addTarget(Bukkit.getConsoleSender());
    ch.setRenderer(MessageRenderer.prefixing(PREFIX));

    ch.filterTargets(audience -> {
      User user = Audiences.getUser(audience);

      if (user == null) {
        return true;
      }

      if (!user.hasPermission(GriefPermissions.EAVESDROP)) {
        return false;
      }

      return user.get(viewProperty);
    });

    return ch;
  }

  public static void reportSign(Player player, Block block, List<Component> lines) {
    if (isEmpty(lines) || player.hasPermission(GriefPermissions.EAVESDROP_ADMIN)) {
      return;
    }

    var pos = WorldVec3i.of(block);

    var ch = createChannelled(EAVES_DROP_SIGN, viewer -> {
      var formatted = Text.vformat(
          """
          &7{0, user} placed a sign at {1, location, -world -clickable}:
          &71)&r {2}
          &72)&r {3}
          &73)&r {4}
          &74)&r {5}""",
          player,
          pos,
          lines.get(0),
          lines.get(1),
          lines.get(2),
          lines.get(3)
      );

      return formatted.create(viewer);
    });

    // Remove player from the list
    ch.filterTargets(audience -> !Audiences.equals(audience, player));
    ch.send();
  }

  private static boolean isEmpty(List<Component> lines) {
    if (lines.isEmpty()) {
      return true;
    }

    for (var c : lines) {
      if (!Text.plain(c).isBlank()) {
        return false;
      }
    }

    return true;
  }

  public static void reportOreMining(Block block, int veinSize, Player player) {
    ChannelledMessage ch = createChannelled(EAVES_DROP_MINING, viewer -> {
      return Text.format(
          "&e{0, user}&r found &e{1, number} {2}&r at &e{3, location, -clickable -world}&r.",
          NamedTextColor.GRAY,
          player,
          veinSize,
          block.getType(),
          block.getLocation()
      );
    });

    // Remove player from the list
    ch.filterTargets(audience -> !Audiences.equals(audience, player));
    ch.send();
  }
}
