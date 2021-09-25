package net.forthecrown.economy.market.guild;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.market.guild.topics.VoteTopicType;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.registry.Registries;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;

import java.util.UUID;

public interface Guilds {
    NamespacedKey VOTE_BOX_KEY = Squire.createFtcKey("vote_box");
    NamespacedKey GUILD_MASTER = Squire.createFtcKey("hazel_guild_master");

    VoteBox VOTE_BOX = new VoteBox(
            new Vector3i(12, 12, 12),
            new Vector3i(12, 12, 12),
            BlockFace.EAST
    );

    CraftPlayerProfile VOTE_BOX_PROFILE = FtcUtils.profileWithTextureID(
            "profile", UUID.randomUUID(),
            "d5f07ab40aa485ac2df62450b7f59b52e8c6589b8101f9bc8ca4a152a838f4e4"
    );

    GuildNPC GUILD_NPC = Util.make(() -> {
        GuildNPC npc = new GuildNPC();
        Registries.NPCS.register(GUILD_MASTER, npc);

        return npc;
    });

    static void validateCanVote(UUID id) throws CommandSyntaxException {
        TradersGuild guild = Crown.getTradersGuild();

        if(!guild.isMember(id)) throw FtcExceptionProvider.translatable("guilds.notMember");
        if(!guild.isCurrentlyVoting()) throw FtcExceptionProvider.translatable("guilds.notVoting");

        if(guild.getVoteState().hasVoted(id)) throw FtcExceptionProvider.translatable("guilds.alreadyVoted");
    }

    static BuiltInventory getVoteSelection() {
        if(Registries.VOTE_TOPICS.size() == 1) {
            for (VoteTopicType t: Registries.VOTE_TOPICS) {
                return t.getInventory();
            }
        }

        InventoryBuilder builder = new InventoryBuilder(36);

        for (VoteTopicType t: Registries.VOTE_TOPICS) {
            builder.add(t.getSelectionOption());
        }

        return builder.build();
    }
}
