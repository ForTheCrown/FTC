package net.forthecrown.guilds.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildPermissionsBook;
import net.forthecrown.guilds.GuildRank;
import net.forthecrown.guilds.unlockables.UnlockableRankSlot;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildRank.ID_MEMBER;
import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

public class GuildRanksMenu extends MenuPage {
    public GuildRanksMenu(MenuPage parent) {
        super(parent);

        initMenu(Menus.builder(45, "Guild Ranks"), true);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        var defaultOption = MenuNode.builder()
                .setItem((user, context) -> {
                    var guild = context.getOrThrow(GUILD);
                    var defaultRank = guild.getSettings()
                            .getRank(ID_MEMBER);

                    return ItemStacks.builder(Material.NAME_TAG)
                            .setName(defaultRank.getName())
                            .setLore(ObjectList.of(
                                    Component.text(defaultRank.getDescription(), NamedTextColor.WHITE)
                                            .decoration(TextDecoration.ITALIC, false),
                                    Component.text("Click to rename", NamedTextColor.GRAY)
                                            .decoration(TextDecoration.ITALIC, false),
                                    Component.text("Shift-click to edit permissions", NamedTextColor.GRAY)
                                            .decoration(TextDecoration.ITALIC, false),
                                    Component.text("RankId: " + defaultRank.getId(), NamedTextColor.DARK_GRAY)
                                            .decoration(TextDecoration.ITALIC, false)))
                            .build();
                })
                .setRunnable((user, context, click) -> {
                    var guild = context.getOrThrow(GUILD);
                    var rank = guild.getSettings()
                            .getRank(ID_MEMBER);

                    onDefaultRanksClick(user, click, rank, guild);
                })
                .build();

        var leaderOption = MenuNode.builder()
                .setItem((user, context) -> {
                    var guild = context.getOrThrow(GUILD);
                    var leaderRank = guild.getSettings()
                            .getRank(ID_LEADER);

                    return ItemStacks.builder(Material.NAME_TAG)
                            .setName(leaderRank.getName())
                            .setLore(ObjectList.of(
                                    Component.text(leaderRank.getDescription(), NamedTextColor.WHITE)
                                            .decoration(TextDecoration.ITALIC, false),
                                    Component.text("Click to rename", NamedTextColor.GRAY)
                                            .decoration(TextDecoration.ITALIC, false),
                                    Component.text("Has all permissions", NamedTextColor.GRAY)
                                            .decoration(TextDecoration.ITALIC, false),
                                    Component.text("RankId: " + leaderRank.getId(), NamedTextColor.DARK_GRAY)
                                            .decoration(TextDecoration.ITALIC, false)))
                            .build();
                })
                .setRunnable((user, context, click) -> {
                    var guild = context.getOrThrow(GUILD);
                    var leaderRank = guild.getSettings()
                            .getRank(ID_LEADER);

                    onDefaultRanksClick(user, click, leaderRank, guild);
                })
                .build();

        builder.add(12, defaultOption)
                .add(14, leaderOption);

        UpgradesMenu.addAll(UnlockableRankSlot.values(), builder);
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return ItemStacks.builder(Material.NAME_TAG)
                .setName("&eGuild Ranks")
                .addLore("&7Information about the ranks in your guild.")
                .build();
    }

    @Override
    protected MenuNode createHeader() {
        return this;
    }

    public static void onDefaultRanksClick(User user, ClickContext click, GuildRank rank, Guild guild)
            throws CommandSyntaxException
    {
        var member = guild.getMember(user.getUniqueId());

        if (user.hasPermission(Permissions.ADMIN)
                || (member != null && member.hasPermission(GuildPermission.CAN_CHANGE_RANKS))
        ) {
            onRankClick(user, guild, click, rank);
        } else {
            throw Exceptions.NO_PERMISSION;
        }
    }

    public static void onRankClick(User user, Guild guild, ClickContext click, GuildRank rank) {
        click.shouldReloadMenu(false);

        var member = guild.getMember(user.getUniqueId());
        if (!user.hasPermission(Permissions.ADMIN)
                && member != null
                && member.getRankId() <= rank.getId()
        ) {
            user.sendMessage(Component.text("You can only edit permission of ranks below yours.", NamedTextColor.RED));
            return;
        }

        if (click.getClickType().isShiftClick()) {
            if (rank.getId() == ID_LEADER) {
                user.sendMessage(Component.text("The leader always has all permissions.", NamedTextColor.RED));
                return;
            }

            GuildPermissionsBook.open(user, guild, rank);
        } else {
            user.getPlayer().closeInventory();

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
                public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                    var oldName = rank.getFormattedName();

                    if (input == null) {
                        return this;
                    }

                    if (BannedWords.checkAndWarn(player, input)) {
                        // Do nothing
                    } else if (input.length() > 20) {
                        user.sendMessage(Text.format("'{0}' is too big, max 20 characters",
                                NamedTextColor.RED,
                                input
                        ));
                    } else {
                        rank.setName(user, input);

                        user.sendMessage(
                                Text.format("Renamed rank '&f{0}&r' to '&f{1}&r'",
                                        NamedTextColor.GRAY,
                                        oldName, rank.getFormattedName()
                                )
                        );
                    }

                    GuildMenus.open(
                            GuildMenus.MAIN_MENU
                                    .getUpgradesMenu()
                                    .getRanksMenu(),

                            user, guild
                    );

                    return Prompt.END_OF_CONVERSATION;
                }
            };

            Conversation conversation = new Conversation(
                    FTC.getPlugin(), player, prompt
            );
            conversation.setLocalEchoEnabled(false);

            player.beginConversation(conversation);
        }
    }
}