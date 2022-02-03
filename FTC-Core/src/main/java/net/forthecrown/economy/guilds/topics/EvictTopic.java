package net.forthecrown.economy.guilds.topics;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.guilds.DelayedVoteTask;
import net.forthecrown.economy.guilds.GuildUtil;
import net.forthecrown.economy.guilds.TradeGuild;
import net.forthecrown.economy.guilds.VoteCount;
import net.forthecrown.economy.guilds.screen.InvPosProvider;
import net.forthecrown.economy.houses.House;
import net.forthecrown.economy.houses.VoteModifier;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.inventory.ItemStackBuilder;
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

public class EvictTopic implements VoteTopic<EvictTopic.EvictionData> {
    public static final Key KEY = Keys.forthecrown("topic_evict");

    EvictTopic() {}

    @Nullable
    @Override
    public DelayedVoteTask onEnd(EvictionData data, VoteCount count) {
        if(!count.isWin()) return null;

        long time = System.currentTimeMillis() + ComVars.getEvictionCleanupTime();
        return new DelayedVoteTask(time, new JsonPrimitive(data.shopName()), this);
    }

    @Override
    public Component extraVictoryText(EvictionData data) {
        return Component.translatable("guilds.topics.evict.extra",
                NamedTextColor.GRAY,

                UserManager.getUser(data.owner()).nickDisplayName()
                        .color(NamedTextColor.YELLOW),

                FtcFormatter.formatDate(ComVars.getEvictionCleanupTime())
                        .color(NamedTextColor.GOLD)
        );
    }

    @Override
    public JsonElement serialize(EvictionData data) {
        return new JsonPrimitive(data.shopName());
    }

    @Override
    public EvictionData deserialize(JsonElement element) {
        return new EvictionData(element.getAsString());
    }

    @Override
    public Component displayText(EvictionData data) {
        MarketShop shop = Crown.getMarkets().get(data.shopName());
        CrownUser user = shop.ownerUser();
        user.unloadIfOffline();

        return Component.translatable("guilds.topics.evict", user.nickDisplayName());
    }

    @Override
    public ItemStack createSelectionScreenItem() {
        ItemStackBuilder builder = new ItemStackBuilder(Material.IRON_AXE, 1)
                .setName(
                        Component.text("Evict a shop owner")
                                .style(FtcFormatter.nonItalic(NamedTextColor.AQUA))
                )
                .addLore(
                        Component.text("Choose a shop owner to evict and then ")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                )
                .addLore(
                        Component.text("vote to evict them")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                );

        return builder.build();
    }

    @Override
    public Collection<? extends CordedInventoryOption> getClickOptions(InvPosProvider provider) {
        List<EvictionOption> options = new ObjectArrayList<>();

        int index = 0;
        for (UUID id: Crown.getMarkets().getOwners()) {
            options.add(new EvictionOption(provider.getPos(index), id));

            index++;
        }

        return options;
    }

    @Override
    public int screenSize() {
        int shopRows = Crown.getMarkets().getOwnedShops().size() / 5;

        if(shopRows <= 1) return 27;
        return (shopRows + 4) * 9;
    }

    @Override
    public Component screenTitle() {
        return Component.text("Select who to evict");
    }

    @Override
    public void runTask(DelayedVoteTask task) {
        String shopName = task.data.getAsString();

        Markets market = Crown.getMarkets();
        MarketShop shop = market.get(shopName);
        if(shop == null || !shop.hasOwner()) return;

        market.unclaim(shop, true);
    }

    @Override
    public VoteModifier createModifier(House h, EvictionData data) {
        return GuildUtil.modFromRelations(data.owner(), h);
    }

    @Override
    public boolean shouldContinueVote(TradeGuild guild, Markets markets, EvictionData data) {
        return markets.get(data.shopName()).hasOwner();
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public record EvictionOption(InventoryPos p, UUID owner) implements CordedInventoryOption {
        @Override
        public InventoryPos getPos() {
            return p;
        }

        public CrownUser user() {
            return UserManager.getUser(owner);
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {
            ItemStackBuilder builder = new ItemStackBuilder(Material.PLAYER_HEAD, 1)
                    .setName(
                            user().coloredNickDisplayName()
                                    .style(FtcFormatter.nonItalic())
                    )
                    .setProfile(UserManager.getUser(owner));

            if(!Crown.getMarkets().get(owner).canBeEvicted()) {
                builder
                        .addLore(
                                user.nickOrName()
                                        .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                                        .append(Component.text(" cannot be evicted right now"))
                        );
            } else {
                builder
                        .addLore(
                                Component.text("Click to start eviction vote!")
                                        .style(FtcFormatter.nonItalic(NamedTextColor.WHITE))
                        )
                        .addEnchant(Enchantment.BINDING_CURSE, 1)
                        .setFlags(ItemFlag.HIDE_ENCHANTS);
            }

            inventory.setItem(getPos(), builder);
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            if(!Crown.getMarkets().get(owner).canBeEvicted()) {
                throw FtcExceptionProvider.translatable("guilds.cannotEvict", UserManager.getUser(owner).nickDisplayName());
            }

            EvictionData data = new EvictionData(Crown.getMarkets().get(owner).getName());

            GuildUtil.confirmVoteStart(data, user);
        }
    }

    public record EvictionData(String shopName) implements VoteData {
        public UUID owner() {
            return Crown.getMarkets().get(shopName).getOwner();
        }

        @Override
        public Key typeKey() {
            return KEY;
        }
    }
}
