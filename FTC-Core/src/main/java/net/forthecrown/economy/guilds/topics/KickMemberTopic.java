package net.forthecrown.economy.guilds.topics;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.guilds.*;
import net.forthecrown.economy.guilds.screen.InvPosProvider;
import net.forthecrown.economy.houses.House;
import net.forthecrown.economy.houses.VoteModifier;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMarketData;
import net.forthecrown.user.UserManager;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class KickMemberTopic implements VoteTopic<KickMemberTopic.KickMemberData> {
    public static final Key KEY = Keys.forthecrown("kick_member");

    KickMemberTopic() {}

    @Nullable
    @Override
    public DelayedVoteTask onEnd(KickMemberData data, VoteCount count) {
        if(count.isWin()) Crown.getGuild().removeMember(data.member());
        UserMarketData marketData = data.memberUser().getMarketData();
        marketData.setKickedFromGuild(System.currentTimeMillis());
        marketData.setGuildJoinDate(0L);

        return null;
    }

    @Override
    public Component extraVictoryText(KickMemberData data) {
        return Component.translatable("guilds.topics.kick.extra",
                NamedTextColor.GRAY,
                data.memberUser().nickDisplayName()
                        .color(NamedTextColor.YELLOW)
        );
    }

    @Override
    public JsonElement serialize(KickMemberData data) {
        return JsonUtils.writeUUID(data.member());
    }

    @Override
    public KickMemberData deserialize(JsonElement element) {
        return new KickMemberData(JsonUtils.readUUID(element));
    }

    @Override
    public Component displayText(KickMemberData data) {
        return Component.text("Kick " + data.memberUser().getNickOrName());
    }

    @Override
    public ItemStack createSelectionScreenItem() {
        ItemStackBuilder builder = new ItemStackBuilder(Material.STONE_SWORD, 1)
                .setName(
                        Component.text("Kick out a guild member")
                                .style(FtcFormatter.nonItalic(NamedTextColor.AQUA))
                )
                .addLore(
                        Component.text("Choose a guild member and then ")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                )
                .addLore(
                        Component.text("vote to kick them from the guild")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                );

        return builder.build();
    }

    @Override
    public Collection<? extends CordedInventoryOption> getClickOptions(InvPosProvider provider) {
        List<KickMemberOption> options = new ObjectArrayList<>();

        int index = 0;
        for (UUID id: Crown.getGuild().getMembers()) {
            options.add(new KickMemberOption(id, provider.getPos(index)));
            index++;
        }

        return options;
    }

    @Override
    public void runTask(DelayedVoteTask task) { }

    @Override
    public VoteModifier createModifier(House h, KickMemberData data) {
        return GuildUtil.modFromRelations(data.member, h);
    }

    @Override
    public boolean shouldContinueVote(TradeGuild guild, Markets markets, KickMemberData data) {
        return guild.isMember(data.member());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public record KickMemberOption(UUID member, InventoryPos pos) implements CordedInventoryOption {
        @Override
        public InventoryPos getPos() {
            return pos;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {
            CrownUser deciding = UserManager.getUser(member);
            ItemStackBuilder builder = new ItemStackBuilder(Material.PLAYER_HEAD, 1)
                    .setName(
                            deciding.coloredNickDisplayName()
                                    .style(FtcFormatter.nonItalic())
                    )
                    .setProfile(deciding);

            if(deciding.getMarketData().canBeKickedFromGuild()) {
                builder
                        .addLore(
                                Component.text("Click to start vote!")
                                        .style(FtcFormatter.nonItalic(NamedTextColor.YELLOW))
                        )
                        .addEnchant(Enchantment.BINDING_CURSE, 1)
                        .setFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                builder.addLore(
                        Component.text("Cannot be kicked yet")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                );
            }

            inventory.setItem(getPos(), builder);
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            CrownUser deciding = UserManager.getUser(member);

            if(!deciding.getMarketData().canBeKickedFromGuild()) {
                throw FtcExceptionProvider.translatable("guilds.cannotKick", deciding.nickDisplayName());
            }

            KickMemberData data = new KickMemberData(member);

            GuildUtil.confirmVoteStart(data, user);
        }
    }

    public record KickMemberData(UUID member) implements VoteData {
        public CrownUser memberUser() {
            return UserManager.getUser(member);
        }

        @Override
        public Key typeKey() {
            return KEY;
        }
    }
}
