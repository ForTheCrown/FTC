package net.forthecrown.commands;

import static net.forthecrown.core.Messages.TOGGLE_BROADCASTS;
import static net.forthecrown.core.Messages.TOGGLE_CHAT_RANKS;
import static net.forthecrown.core.Messages.TOGGLE_DURABILITY_WARN;
import static net.forthecrown.core.Messages.TOGGLE_DYNMAP_HIDE;
import static net.forthecrown.core.Messages.TOGGLE_EAVESDROP_DM;
import static net.forthecrown.core.Messages.TOGGLE_EAVESDROP_GCHAT;
import static net.forthecrown.core.Messages.TOGGLE_EAVESDROP_MCHAT;
import static net.forthecrown.core.Messages.TOGGLE_EAVESDROP_MUTED;
import static net.forthecrown.core.Messages.TOGGLE_EAVESDROP_SIGN;
import static net.forthecrown.core.Messages.TOGGLE_EMOTES;
import static net.forthecrown.core.Messages.TOGGLE_FLY;
import static net.forthecrown.core.Messages.TOGGLE_GODMODE;
import static net.forthecrown.core.Messages.TOGGLE_HULK;
import static net.forthecrown.core.Messages.TOGGLE_INVITE;
import static net.forthecrown.core.Messages.TOGGLE_MARRIAGE;
import static net.forthecrown.core.Messages.TOGGLE_MCHAT;
import static net.forthecrown.core.Messages.TOGGLE_PAYING;
import static net.forthecrown.core.Messages.TOGGLE_PLAYER_RIDING;
import static net.forthecrown.core.Messages.TOGGLE_PROFILE_PRIVATE;
import static net.forthecrown.core.Messages.TOGGLE_TPA;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.SettingsBook;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.user.property.BoolProperty;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.property.UserPreference;
import net.forthecrown.utils.Util;
import org.bukkit.permissions.Permission;

/**
 * A command which toggles a user's {@link BoolProperty} between true and false.
 */
@Getter
public class ToggleCommand extends FtcCommand {

  /**
   * The property this command flips
   */
  private final UserPreference property;

  /**
   * The message format to display when users toggle the property for themselves
   */
  private final String messageFormat;

  /**
   * Command's user-friendly display name
   */
  private final String displayName;

  public ToggleCommand(
      String name,
      String displayName,
      UserPreference property,
      String messageFormat,
      Permission permission,
      String description,
      String... aliases
  ) {
    super(name);

    this.displayName = displayName;
    this.property = property;
    this.messageFormat = messageFormat;
    this.description = description;

    setPermission(permission);
    setAliases(aliases);

    // Adds this option to the
    // settings book
    SettingsBook.addOption(this);

    register();
  }

  /**
   * Sets the state of the property for the given user
   *
   * @param user  The user to set the property of
   * @param state The new value of the property
   * @throws CommandSyntaxException If the user failed {@link #test(User, boolean)} check
   */
  public void setState(User user, boolean state) throws CommandSyntaxException {
    test(user, state);

    property.setState(user, state);
    user.sendMessage(Messages.toggleMessage(messageFormat, state));
  }

  /**
   * Tests if this property is allowed in the {@link SettingsBook} for the given user.
   * <p>
   * This method always returns true, except if this command's property is
   * {@link Properties#MARRIAGE_CHAT} and the user is not married or their spouse is offline.
   *
   * @param user The user to test against
   * @return True, if this option should be displayed in the settings book
   */
  public boolean allowedInBook(User user) {
    return true;
  }

  /**
   * Tests if the user is allowed to change this option to the given state
   * <p>
   * This method will never throw anything, unless the given option is
   * {@link Properties#MARRIAGE_CHAT}, in which case this method will ensure the user is married and
   * that their spouse is online
   *
   * @param user     The user attempting to change the state
   * @param newState The new value of the state
   * @throws CommandSyntaxException If the user fails the test
   */
  public void test(User user, boolean newState) throws CommandSyntaxException {
  }

  @Override
  protected void createCommand(BrigadierCommand command) {
    command
        // /<command>
        .executes(c -> {
          User user = getUserSender(c);
          boolean state = !getProperty().getState(user);

          setState(user, state);
          return 0;
        })

        // /<command> <user>
        .then(argument("user", Arguments.USER)
            .requires(s -> s.hasPermission(Permissions.ADMIN))

            .executes(c -> {
              User user = Arguments.getUser(c, "user");
              boolean state = !getProperty().getState(user);

              test(user, state);

              property.setState(user, state);

              c.getSource().sendAdmin(Messages.toggleOther(displayName, user, state));
              return 0;
            })
        );
  }

  public static void createCommands() {
    new ToggleCommand(
        "profiletoggle",
        "Profile Private",
        Properties.PROFILE_PRIVATE,
        TOGGLE_PROFILE_PRIVATE,
        Permissions.PROFILE,
        "Toggles your profile being private or public",
        "profileprivate", "profilepublic"
    );

    new ToggleCommand(
        "dynmaptoggle",
        "Dynmap Hide",
        UserPreference.DYNMAP_HIDE,
        TOGGLE_DYNMAP_HIDE,
        Permissions.DEFAULT,
        "Toggles others being able to see you on dynmap"
    ) {
      @Override
      public boolean allowedInBook(User user) {
        return DynmapUtil.isInstalled();
      }
    };

    new ToggleCommand(
        "playerridingtoggle",
        "Player Riding",
        UserPreference.PLAYER_RIDING,
        TOGGLE_PLAYER_RIDING,
        Permissions.DEFAULT,
        "Toggles being able to ride other players",
        "ridingtoggle", "playerriding"
    ) {
      @Override
      public boolean allowedInBook(User user) {
        return Util.isPluginEnabled("GSit");
      }
    };

    new ToggleCommand(
        "ignoreac",
        "Broadcasts",
        Properties.IGNORING_ANNOUNCEMENTS,
        TOGGLE_BROADCASTS,
        Permissions.IGNORE_AC,
        "Toggles seeing automated server announcements",
        "ignorebroadcasts", "ignorebc", "ignoreannouncements"
    );

    new ToggleCommand(
        "paytoggle",
        "Payments",
        Properties.PAY,
        TOGGLE_PAYING,
        Permissions.PAY_TOGGLE,
        "Toggles being able to pay and be payed"
    );

    new ToggleCommand(
        "tpatoggle",
        "TPA",
        Properties.TPA,
        TOGGLE_TPA,
        Permissions.TPA,
        "Toggles being able to tpa to people",
        "toggletpa"
    );

    new ToggleCommand(
        "marriagechattoggle",
        "MChat",
        Properties.MARRIAGE_CHAT,
        TOGGLE_MCHAT,
        Permissions.MARRY,
        "Toggles all your messages going to marriage chat",
        "mchattoggle", "mct", "mctoggle"
    ) {
      @Override
      public boolean allowedInBook(User user) {
        if (!user.getInteractions().isMarried()) {
          return false;
        }

        var spouse = user.getInteractions().spouseUser();
        spouse.unloadIfOffline();

        return spouse.isOnline();
      }

      @Override
      public void test(User user, boolean enabled) throws CommandSyntaxException {
        UserInteractions inter = user.getInteractions();

        if (inter.getSpouse() == null) {
          throw Exceptions.NOT_MARRIED;
        }

        if (enabled) {
          User spouse = inter.spouseUser();
          spouse.unloadIfOffline();

          if (!spouse.isOnline()) {
            throw Exceptions.notOnline(spouse);
          }
        }
      }
    };

    new ToggleCommand(
        "toggleguildchat",
        "GC Toggled",
        Properties.G_CHAT_TOGGLE,
        "All your messages will n{1} go to /gc",
        Permissions.GUILD,
        "Toggles all chat messages being sent to /gc",
        "gct", "gctoggle"
    ) {
      @Override
      public boolean allowedInBook(User user) {
        return user.getGuild() != null;
      }

      @Override
      public void test(User user, boolean newState)
          throws CommandSyntaxException
      {
        if (user.getGuild() == null && newState) {
          throw Exceptions.NOT_IN_GUILD;
        }
      }
    };

    new ToggleCommand(
        "toggleemotes",
        "Emotes",
        Properties.EMOTES,
        TOGGLE_EMOTES,
        Permissions.EMOTES,
        "Toggles being able to emote to people and for people to emote at you",
        "emotetoggle"
    );

    new ToggleCommand(
        "marrytoggle",
        "Marrying",
        Properties.ACCEPTING_PROPOSALS,
        TOGGLE_MARRIAGE,
        Permissions.MARRY,
        "Toggles being able to marry and have people send you proposals",
        "togglemarry"
    );

    new ToggleCommand(
        "toggleinvites",
        "Region Invites",
        Properties.REGION_INVITING,
        TOGGLE_INVITE,
        Permissions.WAYPOINTS,
        "Toggles being able to invite and be invited to regions",
        "allowinvites", "denyinvites"
    );

    new ToggleCommand(
        "hulksmash",
        "Hulk Smashing",
        Properties.HULK_SMASHING,
        TOGGLE_HULK,
        Permissions.WAYPOINTS,
        "Toggles whether you quickly teleport to poles or hulk smash onto them",
        "togglehulk", "togglehulksmash"
    );

    new ToggleCommand(
        "godmode",
        "God Mode",
        Properties.GOD,
        TOGGLE_GODMODE,
        Permissions.ADMIN,
        "Toggles god mode",
        "god", "togglegod", "togglegodmode"
    );

    new ToggleCommand(
        "fly",
        "Flying",
        Properties.FLYING,
        TOGGLE_FLY,
        Permissions.ADMIN,
        "Toggles flying"
    );

    new ToggleCommand(
        "itembreakwarning",
        "Item Durability",
        Properties.DURABILITY_ALERTS,
        TOGGLE_DURABILITY_WARN,
        Permissions.DEFAULT,
        "Toggles seeing item breaking warnings",
        "itemdurability", "durabilitywarnings"
    );

    new ToggleCommand(
        "rankchat",
        "Ranks in chat",
        Properties.RANKED_NAME_TAGS,
        TOGGLE_CHAT_RANKS,
        Permissions.DEFAULT,
        "Toggles seeing ranks in chat",
        "chatranks"
    );

    new ToggleCommand(
        "guildrankchat",
        "Rank in gc",
        Properties.GUILD_RANKED_TAGS,
        "N{1} seeing ranks in guild chat",
        Permissions.GUILD,
        "Toggles seeing ranks in guild chat",
        "gcranks", "gchatranks"
    );

    createEavesdrop();
  }

  static void createEavesdrop() {
    new ToggleCommand(
        "eavesdrop_dm",
        "Spy on DMs",
        Properties.EAVES_DROP_DM,
        TOGGLE_EAVESDROP_DM,
        Permissions.ADMIN,
        "Toggles spying on people's DMs",
        "seedms", "see_dms"
    );

    new ToggleCommand(
        "eavesdrop_muted",
        "Spy on muted",
        Properties.EAVES_DROP_MUTED,
        TOGGLE_EAVESDROP_MUTED,
        Permissions.ADMIN,
        "Toggles spying on people's muted messages",
        "seemuted", "see_muted"
    );

    new ToggleCommand(
        "eavesdrop_signs",
        "Spy on signs",
        Properties.EAVES_DROP_SIGN,
        TOGGLE_EAVESDROP_SIGN,
        Permissions.ADMIN,
        "Toggles seeing what people write on signs",
        "seesigns", "see_signs"
    );

    new ToggleCommand(
        "eavesdrop_mchat",
        "Spy on mchat",
        Properties.EAVES_DROP_MCHAT,
        TOGGLE_EAVESDROP_MCHAT,
        Permissions.ADMIN,
        "Toggles seeing what people say in marriage dms",
        "seemchat", "seemarriage", "see_mchat", "see_marriage"
    );

    new ToggleCommand(
        "eavesdrop_gchat",
        "Spy on guilds",
        Properties.EAVES_DROP_GUILD_CHAT,
        TOGGLE_EAVESDROP_GCHAT,
        Permissions.ADMIN,
        "Toggles seeing guild chats"
    );
  }
}