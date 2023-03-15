package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildRank.ID_MEMBER;
import static net.forthecrown.guilds.GuildRank.NOT_SET;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildPermissionsBook;
import net.forthecrown.guilds.GuildRank;
import net.forthecrown.guilds.unlockables.UnlockableRankSlot;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.ClickContext;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildRanksMenu extends MenuPage {

  public GuildRanksMenu(MenuPage parent) {
    super(parent);

    initMenu(Menus.builder(45, "Guild Ranks"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(12, UnlockableRankSlot.rankNode(ID_MEMBER, null))
        .add(14, UnlockableRankSlot.rankNode(ID_LEADER, null));

    UpgradesMenu.addAll(builder, UnlockableRankSlot.values());
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.NAME_TAG)
        .setName("&eGuild Ranks")
        .addLore("&7Information about the ranks in your guild.")
        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }

  public static void onDefaultRanksClick(User user,
                                         ClickContext click,
                                         GuildRank rank,
                                         Guild guild
  ) throws CommandSyntaxException {
    var member = guild.getMember(user.getUniqueId());

    if (user.hasPermission(Permissions.ADMIN)
        || (member != null && member.hasPermission(GuildPermission.CAN_CHANGE_RANKS))
    ) {
      onRankClick(user, guild, click, rank);
    } else {
      throw Exceptions.NO_PERMISSION;
    }
  }

  public static void onRankClick(User user,
                                 Guild guild,
                                 ClickContext click,
                                 GuildRank rank
  ) throws CommandSyntaxException {
    click.shouldReloadMenu(false);
    click.cancelEvent(true);

    checkPermission(user, guild, rank);

    switch (click.getClickType()) {
      case SHIFT_LEFT  -> changePermissions(user, guild, rank);
      case SHIFT_RIGHT -> changeAutoLevelUp(user, guild, rank);
      case LEFT        -> changeRankName(user, guild, rank);
      case RIGHT       -> changeMaxChunkClaims(user, guild, rank);
    }
  }

  private static void checkPermission(User user, Guild guild, GuildRank rank)
      throws CommandSyntaxException
  {
    if (user.hasPermission(Permissions.GUILD_ADMIN)) {
      return;
    }

    GuildMember member = guild.getMember(user.getUniqueId());

    if (member == null) {
      throw Exceptions.notGuildMember(guild);
    }

    var rankId = member.getRankId();
    if (rankId > rank.getId() || rankId == ID_LEADER) {
      return;
    }

    throw Exceptions.create(
        "You can only edit permission of ranks below yours."
    );
  }

  private static void changeMaxChunkClaims(User user,
                                           Guild guild,
                                           GuildRank rank
  ) throws CommandSyntaxException {
    if (rank.getId() == ID_LEADER) {
      throw Exceptions.create("Cannot change this for the guild leader");
    }

    conversationRankEdit(user, new MaxChunkClaimsPrompt(user, guild, rank));
  }

  private static void changeAutoLevelUp(User user,
                                        Guild guild,
                                        GuildRank rank
  ) throws CommandSyntaxException {
    if (rank.getId() == ID_LEADER) {
      throw Exceptions.create("Cannot change this for the guild leader");
    }

    conversationRankEdit(user, new AutoLevelUpPrompt(user, guild, rank));
  }

  private static void changePermissions(User user,
                                        Guild guild,
                                        GuildRank rank
  ) throws RoyalCommandException {
    if (rank.getId() == ID_LEADER) {
      throw Exceptions.create("The leader always has all permissions.");
    }

    GuildPermissionsBook.open(user, guild, rank);
  }

  private static void changeRankName(User user, Guild guild, GuildRank rank) {
    Component info = Component.text("Type the new name for ")
        .color(NamedTextColor.YELLOW)
        .append(rank.getFormattedName());

    user.sendActionBar(info);
    user.sendMessage(info
        .append(Component.space())
        .append(Component.text("[✖]", NamedTextColor.GRAY)
            .hoverEvent(Component.text("Cancel renaming the rank by")
                .append(Component.newline())
                .append(Component.text("entering the current name."))
            )

            .clickEvent(ClickEvent.suggestCommand(
                rank.getName()
            ))
        )
    );

    var player = user.getPlayer();
    StringPrompt prompt = new StringPrompt() {
      @Override
      public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return "";
      }

      @Override
      public @Nullable Prompt acceptInput(@NotNull ConversationContext context,
                                          @Nullable String input
      ) {
        var oldName = rank.getName();

        if (input == null) {
          return this;
        }

        if (BannedWords.checkAndWarn(player, input)) {
          return openRankMenu(user, guild);
        }

        if (input.length() > 20) {
          user.sendMessage(Text.format("'{0}' is too big, max 20 characters",
              NamedTextColor.RED,
              input
          ));

          return openRankMenu(user, guild);
        }

        rank.setName(input);

        if (oldName.equals(input)) {
          user.sendMessage(Messages.NOTHING_CHANGED);
          return openRankMenu(user, guild);
        }

        sendGuildAnnouncement(
            guild, user,

            Text.format(
                "&e{0, user}&r has renamed rank '&f{1}&r' to '&f{2}&r'",
                NamedTextColor.GRAY,
                user, Text.renderString(oldName), rank.getFormattedName()
            ),

            () -> Text.format("Renamed rank '&f{0}&r' to '&f{1}&r'",
                NamedTextColor.GRAY,
                oldName, rank.getFormattedName()
            )
        );

        return openRankMenu(user, guild);
      }
    };

    conversationRankEdit(user, prompt);
  }

  private static void conversationRankEdit(User user, Prompt prompt) {
    var player = user.getPlayer();

    // Gives you the item you clicked on in some circumstances, delay
    // closing by a tick to prevent
    Tasks.runLater(player::closeInventory, 1);

    Conversation conversation
        = new Conversation(FTC.getPlugin(), player, prompt);

    conversation.setLocalEchoEnabled(false);

    player.beginConversation(conversation);
  }

  private static Prompt openRankMenu(User user, Guild guild) {
    GuildMenus.open(
        GuildMenus.MAIN_MENU
            .getUpgradesMenu()
            .getRanksMenu(),

        user, guild
    );

    return Prompt.END_OF_CONVERSATION;
  }

  private static void sendGuildAnnouncement(Guild guild,
                                            User user,
                                            Component announcement,
                                            Supplier<Component> adminMessage
  ) {
    guild.announce(announcement);

    if (!guild.isMember(user.getUniqueId())) {
      user.sendMessage(adminMessage.get());
    }
  }

  @RequiredArgsConstructor
  private static abstract class RankEditPrompt extends NumericPrompt {

    final User user;
    final Guild guild;
    final GuildRank rank;

    @Override
    protected @Nullable Prompt acceptValidatedInput(
        @NotNull ConversationContext context,
        @NotNull Number input
    ) {
      int current = get(rank);
      int value = input.intValue();

      if (current == value || (value <= 0 && current == NOT_SET)) {
        user.sendMessage(Messages.NOTHING_CHANGED);
        return openRankMenu(user, guild);
      }

      if (value < 0) {
        set(rank, NOT_SET);

        sendGuildAnnouncement(
            guild, user,

            announcement(NOT_SET),
            () -> adminMessage(NOT_SET)
        );

        return openRankMenu(user, guild);
      }

      set(rank, value);

      sendGuildAnnouncement(
          guild, user,

          announcement(value),
          () -> announcement(value)
      );

      return openRankMenu(user, guild);
    }

    protected abstract int get(GuildRank rank);
    protected abstract void set(GuildRank rank, int value);

    protected abstract Component announcement(int value);
    protected abstract Component adminMessage(int value);
  }

  private static class AutoLevelUpPrompt extends RankEditPrompt {

    public AutoLevelUpPrompt(User user, Guild guild, GuildRank rank) {
      super(user, guild, rank);
    }

    @Override
    protected int get(GuildRank rank) {
      return rank.getTotalExpLevelUp();
    }

    @Override
    protected void set(GuildRank rank, int value) {
      rank.setTotalExpLevelUp(value);
    }

    @Override
    protected Component announcement(int value) {
      if (value == NOT_SET) {
        return Text.format("&e{0, user}&r removed rank &f{1}&r's auto level up.",
            NamedTextColor.GRAY,
            user, rank.getFormattedName()
        );
      }

      return Text.format(
          "&e{0, user}&r set rank &f{1}&r's auto level-up to &e{2, number}&r.",
          NamedTextColor.GRAY,
          user, rank.getFormattedName(), value
      );
    }

    @Override
    protected Component adminMessage(int value) {
      if (value == NOT_SET) {
        return Text.format("Removed rank &f{0}&r's auto level up.",
            NamedTextColor.GRAY,
            rank.getFormattedName()
        );
      }

      return Text.format(
          "Set rank &f{0}&r's auto level-up to &e{1, number}&r.",
          NamedTextColor.GRAY,
          rank.getFormattedName(), value
      );
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
      return "Enter rank auto level up exp requirement "
          + "(any value below 0 will remove the auto level-up):";
    }
  }

  private static class MaxChunkClaimsPrompt extends RankEditPrompt {

    public MaxChunkClaimsPrompt(User user, Guild guild, GuildRank rank) {
      super(user, guild, rank);
    }

    @Override
    protected int get(GuildRank rank) {
      return rank.getMaxChunkClaims();
    }

    @Override
    protected void set(GuildRank rank, int value) {
      rank.setMaxChunkClaims(value);
    }

    @Override
    protected Component announcement(int value) {
      if (value == NOT_SET) {
        return Text.format(
            "&f{0, user}&r removed max chunk claim limit from rank &f{1}&r.",
            NamedTextColor.GRAY,
            user, rank.getFormattedName()
        );
      }

      return Text.format(
          "&e{0, user}&r set max chunk claim limit on rank &f{1}&r "
              + "to &e{2, number}&r.",
          NamedTextColor.GRAY,
          user, rank.getFormattedName(), value
      );
    }

    @Override
    protected Component adminMessage(int value) {
      if (value == NOT_SET) {
        return Text.format(
            "Removed max chunk claim limit from rank &f{0}&r.",
            NamedTextColor.GRAY,
            rank.getFormattedName()
        );
      }

      return Text.format(
          "Set max chunk claim limit on rank &f{0}&r to &e{1, number}&r.",
          NamedTextColor.GRAY,
          rank.getFormattedName(), value
      );
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
      return "Type max chunk claim limit "
          + "(any value below 0 will remove the limit):";
    }
  }
}