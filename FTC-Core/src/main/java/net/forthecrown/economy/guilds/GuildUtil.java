package net.forthecrown.economy.guilds;

import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.guilds.topics.VoteData;
import net.forthecrown.economy.guilds.topics.VoteTopic;
import net.forthecrown.economy.houses.House;
import net.forthecrown.economy.houses.Relation;
import net.forthecrown.economy.houses.VoteModifier;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public final class GuildUtil {
    private GuildUtil() {}

    public static VoteModifier modFromRelations(UUID uuid, House h) {
        return new VoteModifier(-(h.getRelationWith(uuid).getValue() / (Relation.MAX_RELATION / VoteModifier.MAX_VALUE)));
    }

    public static Component display(VoteData data) {
        VoteTopic<VoteData> topic = Registries.VOTE_TOPICS.get(data.typeKey());
        return topic.displayText(data);
    }

    public static void confirmVoteStart(VoteData data, CrownUser usr) {
        usr.getPlayer().closeInventory();

        String name = "confirm_vote_" + data.typeKey() + "_" + usr.getName();
        ClickableTextNode node = new ClickableTextNode(name)
                .setPrompt(user -> {
                    Component prompt = Component.translatable("guilds.voteStart.confirm1", NamedTextColor.YELLOW);

                    if(!Crown.getGuild().canStartVote()) {
                        return prompt
                                .color(NamedTextColor.GRAY)
                                .hoverEvent(Component.translatable("guilds.voteStart.error"));
                    }

                    return prompt.color(NamedTextColor.AQUA);
                })
                .setExecutor(user -> {
                    startVote(data, user);
                    ClickableTexts.unregister(name);
                });

        ClickableTexts.register(node);
        Component prompt = node.prompt(usr);

        usr.sendMessage(
                Component.translatable("guilds.voteStart.confirm", NamedTextColor.GRAY, display(data))
                        .append(Component.newline())
                        .append(prompt)
        );
    }

    public static void startVote(VoteData data, CrownUser user) throws RoyalCommandException {
        TradeGuild guild = Crown.getGuild();

        if(!guild.canStartVote()) {
            throw FtcExceptionProvider.translatable("guilds.voteStart.error");
        }

        Crown.getGuild().createVote(data, user.getUniqueId());
    }

    public static Component extraVictoryText(VoteData data, VoteTopic topic, VoteCount count) {
        if(!count.isWin()) return Component.empty();
        Component result = topic.extraVictoryText(data);

        return result == null ? Component.empty() : result;
    }
}