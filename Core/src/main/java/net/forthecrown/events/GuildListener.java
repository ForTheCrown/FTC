package net.forthecrown.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.clickevent.ClickEventManager;
import net.forthecrown.commands.clickevent.ClickEventTask;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.guild.GuildVoter;
import net.forthecrown.economy.guild.Guilds;
import net.forthecrown.economy.guild.TradersGuild;
import net.forthecrown.economy.guild.VoteState;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GuildListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        BlockState state = event.getClickedBlock().getState();
        if(!(state instanceof Skull)) return;

        Skull skull = (Skull) state;
        PersistentDataContainer container = skull.getPersistentDataContainer();
        if(container.isEmpty() || !container.has(Guilds.VOTE_BOX_KEY, PersistentDataType.BYTE)) return;

        Events.handlePlayer(event, event1 -> {
            CrownUser user = UserManager.getUser(event.getPlayer());
            Guilds.validateCanVote(user.getUniqueId());

            TradersGuild guild = Crown.getTradersGuild();
            UserVoter voter = new UserVoter(user);

            guild.getVoteState().vote(voter);
        });
    }

    private static class UserVoter implements GuildVoter, ClickEventTask {
        private final CrownUser user;
        private VoteState state;
        private final String npcID;

        private UserVoter(CrownUser user) {
            this.user = user;

            npcID = ClickEventManager.registerClickEvent(this);
        }

        @Override
        public void vote(VoteState state) {
            this.state = state;

            ClickEventManager.allowCommandUsage(user.getPlayer(), true, false);

            user.sendMessage(
                    Component.translatable("guilds.vote.prompt",
                            NamedTextColor.GRAY,
                            state.getTopic().signDisplay().color(NamedTextColor.YELLOW),

                            Component.translatable("guilds.vote.yes", NamedTextColor.GREEN)
                                    .clickEvent(ClickEventManager.getClickEvent(npcID, "for"))
                                    .hoverEvent(Component.translatable("guilds.vote.yes.hover")),

                            Component.translatable("guilds.vote.no", NamedTextColor.RED)
                                    .clickEvent(ClickEventManager.getClickEvent(npcID, "against"))
                                    .hoverEvent(Component.translatable("guilds.vote.no.hover"))
                    )
            );
        }

        @Override
        public void run(Player player, String[] args) throws CommandSyntaxException {
            ClickEventManager.unregisterClickEvent(npcID);
            Guilds.validateCanVote(player.getUniqueId());

            if(args.length < 2) return;
            String argument = args[1];

            if(argument.equalsIgnoreCase("for")) {
                state.voteFor(player.getUniqueId());

                player.sendMessage(
                        Component.translatable("guilds.voted.for",
                                NamedTextColor.GRAY,
                                state.getTopic().signDisplay().color(NamedTextColor.YELLOW)
                        )
                );
            } else {
                state.voteAgainst(player.getUniqueId());

                player.sendMessage(
                        Component.translatable("guilds.voted.against",
                                NamedTextColor.GRAY,
                                state.getTopic().signDisplay().color(NamedTextColor.YELLOW)
                        )
                );
            }
        }
    }
}
