package net.forthecrown.guilds;

import java.util.UUID;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.kyori.adventure.identity.Identity;

public class GUserProperties {

  public static final UserProperty<MemberSort> MEMBER_SORT
      = Properties.enumProperty("memberSort", MemberSort.BY_RANK);

  public static final UserProperty<DiscoverySort> DISCOVERY_SORT
      = Properties.enumProperty("guildDiscovery", DiscoverySort.BY_NAME);

  public static final UserProperty<Boolean> DISCOVERY_SORT_INVERTED
      = Properties.booleanProperty("guildDiscoverySortInverted", false);

  public static final UserProperty<Boolean> DISCOVERY_ONLY_PUBLIC
      = Properties.booleanProperty("guildDiscovery_onlyPublic", true);

  public static final UserProperty<Boolean> G_CHAT_TOGGLE
      = Properties.booleanProperty("guildChatToggled", false);

  public static final UserProperty<Boolean> GUILD_RANKED_TAGS
      = Properties.booleanProperty("guildRankedNameTags", false);

  public static final UserProperty<UUID> GUILD = Properties.uuidProperty()
      .key("guild")
      .defaultValue(Identity.nil().uuid())
      .build();

  static void init(SettingsBook<User> settingsBook) {
    Setting chatToggle = Setting.create(G_CHAT_TOGGLE)
        .setDisplayName("GuildChat")
        .setDescription("Toggles all chat messages going to your guild's chat")
        .setToggle("All chat messages will n{1} go to your guild chat")
        .setToggleDescription("{Enable} all chat messages going to your guild's chat")

        .createCommand(
            "guildchattoggle",
            GuildPermissions.GUILD,
            GuildPermissions.GUILD_ADMIN,
            "gct", "gchattoggle", "gctoggle"
        );

    Setting rankedNameTags = Setting.create(GUILD_RANKED_TAGS)
        .setDisplayName("Ranks in GC")
        .setDescription("Toggles player names being prefixed with their guild rank in Guild Chat")
        .setToggle("N{1} seeing ranks in guild chat")
        .setToggleDescription("{Enable} Rank tags in Guild Chat")

        .createCommand(
            "guildrankchat",
            GuildPermissions.GUILD,
            GuildPermissions.GUILD_ADMIN,
            "gcranks", "gchatranks"
        );

    settingsBook.getSettings().add(rankedNameTags.toBookSettng());
    settingsBook.getSettings().add(chatToggle.toBookSettng());
  }
}
