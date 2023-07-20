package net.forthecrown.antigrief;

import static net.forthecrown.discord.FtcDiscord.findChannel;

import com.mojang.datafixers.util.Either;
import github.scarsz.discordsrv.dependencies.jda.api.entities.IMentionable;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingAccess;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.discord.FtcDiscord;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.ChannelledMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;

/**
 * Class representing the staff chat
 * <p>Exists because I was cleaning up the ChatEvents class lol</p>
 */
public final class StaffChat {
  private StaffChat() {}

  private static final String COOL_CLUB = "cool-club";

  public static final Set<UUID> toggledPlayers = new HashSet<>();

  public static final Component PREFIX
      = Component.text("[Staff] ", NamedTextColor.DARK_GRAY);

  public static final Component VANISH_PREFIX
      = Component.text("[VANISH] ", NamedTextColor.WHITE);

  public static final Component DISCORD_PREFIX
      = Component.text("[Discord] ", NamedTextColor.GRAY);

  static void createSettings(SettingsBook<User> settingsBook) {
    SettingAccess access = new SettingAccess() {
      @Override
      public boolean getState(User user) {
        return toggledPlayers.contains(user.getUniqueId());
      }

      @Override
      public void setState(User user, boolean state) {
        if (state) {
          toggledPlayers.add(user.getUniqueId());
        } else {
          toggledPlayers.remove(user.getUniqueId());
        }
      }
    };

    Setting setting = Setting.create(access)
        .setDescription("Toggles all chat message being sent to staff chat")
        .setDisplayName("SC Toggle")
        .setToggleDescription("{Enable} all chat messages being sent to staff chat")
        .setToggle("N{1} sending all chat messages to Staff Chat")

        .createCommand(
            "staffchattoggle",
            GriefPermissions.STAFF_CHAT,
            GriefPermissions.STAFF_CHAT,
            "sct", "sctoggle"
        );

    settingsBook.getSettings().add(setting.toBookSettng());
  }

  public static boolean isVanished(CommandSource source) {
    return source != null
        && source.isPlayer()
        && Users.get(source.asPlayerOrNull()).get(Properties.VANISHED);
  }

  public static StaffChatMessage newMessage() {
    return new StaffChatMessage();
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  public static class StaffChatMessage {
    private MessageSource source;

    private ViewerAwareMessage message;
    private Component prefix;

    private boolean logged;
    private boolean discordForwarded = true;
    private boolean fromDiscord;

    public StaffChatMessage setSource(MessageSource source) {
      this.source = source;
      return this;
    }

    public StaffChatMessage setSource(Member source) {
      return setSource(MessageSource.of(source));
    }

    public StaffChatMessage setSource(CommandSource source) {
      return setSource(MessageSource.of(source));
    }

    public StaffChatMessage setSource(String sourceName) {
      return setSource(MessageSource.simple(sourceName));
    }

    public void send() {
      Objects.requireNonNull(message, "Message not specified");

      ChannelledMessage msg = ChannelledMessage.create(viewer -> {
        var writer = TextWriters.newWriter();
        writer.viewer(viewer);

        write(writer);
        return writer.asComponent();
      });

      Bukkit.getOnlinePlayers().forEach(player -> {
        if (!player.hasPermission(GriefPermissions.STAFF_CHAT)) {
          return;
        }

        msg.addTarget(player);
      });

      if (isLogged()) {
        msg.addTarget(Bukkit.getConsoleSender());
      }

      msg.send();

      if (discordForwarded && !fromDiscord) {
        sendDiscord();
      }
    }

    private void sendDiscord() {
      findChannel(COOL_CLUB).ifPresent(channel -> {
        String strMessage = Text.toDiscord(message.asComponent());

        channel.sendMessageFormat("**%s >** %s",
            source == null
                ? "UNKNOWN"
                : Text.toDiscord(source.displayName(null)),
            strMessage
        ).submit();
      });
    }

    public void write(TextWriter writer) {
      writer.write(PREFIX);

      if (isFromDiscord()) {
        writer.write(DISCORD_PREFIX);
      }

      if (prefix != null) {
        writer.write(prefix);
      }

      if (source != null) {
        if (source.isVanished()) {
          writer.write(VANISH_PREFIX);
        }

        writer.write(source.displayName(writer.viewer()));
      }

      writer.write(
          Component.text(" > ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
      );

      writer.write(message.create(writer.viewer()));
    }
  }

  public interface MessageSource {
    Component displayName(Audience viewer);

    boolean isVanished();

    String mentionString();

    static MessageSource simple(String name) {
      return new MessageSource() {
        @Override
        public Component displayName(Audience viewer) {
          return Component.text(name);
        }

        @Override
        public boolean isVanished() {
          return false;
        }

        @Override
        public String mentionString() {
          return null;
        }
      };
    }

    static MessageSource of(CommandSource source) {
      return new MessageSource() {
        @Override
        public Component displayName(Audience viewer) {
          if (source.isPlayer()) {
            return Users.get(source.asPlayerOrNull()).displayName(viewer);
          }

          return source.displayName();
        }

        @Override
        public boolean isVanished() {
          return StaffChat.isVanished(source);
        }

        @Override
        public String mentionString() {
          if (!source.isPlayer()) {
            return null;
          }

          return FtcDiscord.getUserMember(source.asPlayerOrNull().getUniqueId())
              .map(IMentionable::getAsMention)
              .orElse(null);
        }
      };
    }

    static MessageSource of(Member member) {
      return new MessageSource() {

        Either<User, Member> asUser() {
          UUID playerId = FtcDiscord.getPlayerId(member);

          if (playerId == null) {
            return Either.right(member);
          }

          var user = Users.get(playerId);
          return Either.left(user);
        }

        @Override
        public Component displayName(Audience viewer) {
          return asUser().map(
              user -> user.displayName(viewer),
              member1 -> Component.text(member1.getEffectiveName())
          );
        }

        @Override
        public String mentionString() {
          return member.getAsMention();
        }

        @Override
        public boolean isVanished() {
          return asUser().map(
              user -> user.get(Properties.VANISHED),
              member1 -> false
          );
        }
      };
    }
  }
}