package net.forthecrown.economy.market.guild;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.click.TextNode;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMarketOwnership;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class GuildNPC implements InteractableNPC {
    GuildNPC() {}

    private final TextNode guildInfo = new TextNode("info_guild")
            .setExecutor(user -> {
                user.sendMessage("To keep the market fresh, this group of players can close inactive shops.",
                        "To join them, you must have had a shop in this region for atleast 6 months.",
                        "There are max 5 (?) members in this guild, their shops get some benefits as a reward.");
            })
            .setPrompt(user -> Component.translatable("guildmaster.infoGuild"));

    private final TextNode marketInfo = new TextNode("info_market")
            .setExecutor(user -> {
                user.sendMessage("Houses in this region can be used as player shops.",
                        "Add members to your shop using these commands:",
                        "/shoptrust <player> to allow the player to access your shops",
                        "/mergeshop <player> to merge two shops into one",
                        "Inactive shops can be closed down by the market guild.");
            })
            .setPrompt(user -> Component.translatable("guildmaster.infoMarket"));

    private final TextNode proposeMotion = new TextNode("propose_motion")
            .setExecutor(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                validateMember(guild, user.getUniqueId());

                if(!guild.canStartVote()) {
                    throw FtcExceptionProvider.translatable("guildmaster.voteMenu.notYet");
                }

                Guilds.getVoteSelection().open(user);
            })
            .setPrompt(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                if(!guild.isMember(user.getUniqueId())) return null;
                Component prompt = Component.translatable("guildmaster.voteMenu");

                if(!guild.canStartVote()) {
                    return prompt
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(Component.translatable("guildmaster.voteMenu.notYet"));
                }

                return prompt.color(NamedTextColor.YELLOW);
            });

    private final TextNode joinGuild = new TextNode("join_guild")
            .setExecutor(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                if(guild.isMember(user.getUniqueId())) return;

                UserMarketOwnership ownership = user.getMarketOwnership();

                if(guild.getMembers().size() >= guild.getMaxMembers()) {
                    throw FtcExceptionProvider.translatable("guilds.join.error.memberLimit");
                }

                if(!ownership.currentlyOwnsShop()) {
                    throw FtcExceptionProvider.translatable("guilds.join.error.noShopOwned");
                }

                Date validJoin = new Date(ownership.getOwnershipBegan() + ComVars.getGuildJoinRequirement());
                Date current = new Date();

                if(validJoin.after(current)) {
                    throw FtcExceptionProvider.translatable("guilds.join.error.shopOwnDuration", FtcFormatter.millisIntoTime(ComVars.getGuildJoinRequirement()));
                }

                guild.addMember(user.getUniqueId());
                user.sendMessage(
                        Component.translatable()
                                .key("guilds.join")
                                .color(NamedTextColor.YELLOW)

                                .append(Component.newline())
                                .append(Component.translatable("guilds.join2").color(NamedTextColor.GRAY))

                                .build()
                );

                Component broadcast = Component.translatable("guilds.join.broadcast",
                        user.nickDisplayName()
                                .color(NamedTextColor.GOLD)
                ).color(NamedTextColor.YELLOW);

                guild.forEachUser(u -> u.sendOrMail(broadcast));
            })
            .setPrompt(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                if(guild.isMember(user.getUniqueId())) return null;

                UserMarketOwnership ownership = user.getMarketOwnership();

                Component prompt = Component.translatable("guildmaster.join");

                if(guild.getMembers().size() >= guild.getMaxMembers()) {
                    return prompt
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(Component.translatable("guilds.join.error.memberLimit"));
                }

                if(!ownership.currentlyOwnsShop()) {
                    return prompt
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(Component.translatable("guilds.join.error.noShopOwned"));
                }

                Date validJoin = new Date(ownership.getOwnershipBegan() + ComVars.getGuildJoinRequirement());
                Date current = new Date();

                if(validJoin.after(current)) {
                    return prompt
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(Component.translatable("guilds.join.error.shopOwnDuration", FtcFormatter.millisIntoTime(ComVars.getGuildJoinRequirement())));
                }

                return prompt.color(NamedTextColor.YELLOW);
            });

    private final TextNode leaveGuildConfirm = new TextNode("confirm_leave_guild")
            .setExecutor(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                validateMember(guild, user.getUniqueId());

                guild.removeMember(user.getUniqueId());

                user.sendMessage(
                        Component.translatable("guilds.left", NamedTextColor.YELLOW)
                );

                guild.forEachUser(u -> u.sendOrMail(
                        Component.translatable("guilds.left.others",
                                NamedTextColor.GRAY,
                                user.nickDisplayName().color(NamedTextColor.YELLOW)
                        )
                ));
            })
            .setPrompt(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                if(!guild.isMember(user.getUniqueId())) return null;

                return Component.translatable("guildmaster.leave.confirm", NamedTextColor.YELLOW);
            });

    private final TextNode leaveGuild = new TextNode("leave_guild")
            .addNode(leaveGuildConfirm)
            .setExecutor(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                validateMember(guild, user.getUniqueId());

                user.sendMessage(
                        Component.translatable("guildmaster.leave.confirm.question", NamedTextColor.YELLOW)
                                .append(Component.newline())
                                .append(leaveGuildConfirm.prompt(user))
                );
            })
            .setPrompt(user -> {
                TradersGuild guild = Crown.getTradersGuild();
                if(!guild.isMember(user.getUniqueId())) return null;

                return Component.translatable("guildmaster.leave", NamedTextColor.YELLOW);
            });

    private final TextNode baseNode = ClickableTexts.register(
            new TextNode("guild_npc")
                    .addNode(leaveGuild)
                    .addNode(joinGuild)
                    .addNode(proposeMotion)
                    .addNode(guildInfo)
                    .addNode(marketInfo)
    );

    @Override
    public void run(Player player, Entity entity) throws CommandSyntaxException {
        CrownUser user = UserManager.getUser(player);

        user.sendMessage(
                Component.translatable()
                        .key("guildmaster.initialText")
                        .args(user.nickDisplayName().color(NamedTextColor.GOLD))
                        .color(NamedTextColor.YELLOW)

                        .append(Component.newline())
                        .append(baseNode.presentPrompts(user))
                        .build()
        );
    }

    private void validateMember(TradersGuild guild, UUID id) throws RoyalCommandException {
        if(!guild.isMember(id)) {
            throw FtcExceptionProvider.translatable("guilds.notMember");
        }
    }
}
