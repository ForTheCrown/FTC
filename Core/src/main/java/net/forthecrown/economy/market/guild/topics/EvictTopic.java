package net.forthecrown.economy.market.guild.topics;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.houses.Dynasty;
import net.forthecrown.economy.houses.Relation;
import net.forthecrown.economy.houses.VoteModifier;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.guild.VoteResult;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public class EvictTopic implements VoteTopicType<EvictTopic.TopicInstance> {
    public static final Key KEY = Crown.coreKey("evict_owner");
    public static final EvictTopic INSTANCE = new EvictTopic();

    @Override
    public TopicInstance deserialize(JsonElement element) {
        return new TopicInstance(element.getAsString());
    }

    @Override
    public JsonElement serialize(TopicInstance value) {
        return new JsonPrimitive(value.getShop());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public Component signDisplay(TopicInstance value) {
        CrownUser user = value.user();
        user.unloadIfOffline();

        return Component.translatable("guilds.topics.evict", user.nickDisplayName());
    }

    @Override
    public void runTask(PostVoteTask task) {
        String shopName = task.data.getAsString();

        Markets market = Crown.getMarkets();
        MarketShop shop = market.get(shopName);
        if(shop == null) return;

        market.unclaim(shop, true);
    }

    @Override
    public BuiltInventory getInventory() {
        Markets markets = Crown.getMarkets();
        Collection<MarketShop> shops = markets.getOwnedShops();

        InventoryBuilder builder = new InventoryBuilder(shops.size() > 14 ? 54 : 36)
                .title(Component.text("Choose who to evict"));

        int index = 0;
        for (MarketShop s: shops) {
            InventoryPos pos = posFromIndex(index);
            index++;

            EvictHeadOption option = new EvictHeadOption(pos, s);
            builder.add(option);
        }

        return builder.build();
    }

    private InventoryPos posFromIndex(int index) {
        int y = index / 6;
        int x = index % 6;

        return new InventoryPos(x + 1, y + 1);
    }

    @Override
    public CordedInventoryOption getSelectionOption() {
        return EvictOwnerOption.INSTANCE;
    }

    public static class EvictOwnerOption implements CordedInventoryOption {
        public static final EvictOwnerOption INSTANCE = new EvictOwnerOption();
        private final InventoryPos pos = new InventoryPos(4, 1);

        @Override
        public InventoryPos getPos() {
            return pos;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {
            inventory.setItem(
                    pos,

                    new ItemStackBuilder(Material.BARRIER, 1)
                            .setName(Component.text("Evict a shop owner").style(nonItalic(NamedTextColor.YELLOW)))

                            .addLore(Component.text("Force a shop owner to give up their shop").style(nonItalic(NamedTextColor.GRAY)))
                            .addLore(Component.text("For any reason").style(nonItalic(NamedTextColor.GRAY)))

                            .build()
            );
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            EvictTopic.INSTANCE.getInventory().open(user);
        }
    }

    private static class EvictHeadOption implements CordedInventoryOption {
        private final InventoryPos pos;
        private final MarketShop market;

        private EvictHeadOption(InventoryPos pos, MarketShop market) {
            this.pos = pos;
            this.market = market;
        }

        public MarketShop getShop() {
            return market;
        }

        @Override
        public InventoryPos getPos() {
            return pos;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {
            CrownUser head = market.ownerUser();

            ItemStackBuilder builder = new ItemStackBuilder(Material.PLAYER_HEAD, 1)
                    .setProfile(head)
                    .setName(head.nickDisplayName().style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)));

            if(market.canBeEvicted()) {
                builder
                        .addEnchant(Enchantment.CHANNELING, 1)
                        .setFlags(ItemFlag.HIDE_ENCHANTS);
            }

            inventory.setItem(pos, builder);
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            CrownUser owner = market.ownerUser();

            if(!market.canBeEvicted()) {
                throw FtcExceptionProvider.translatable("guilds.cannotEvict", owner.nickDisplayName());
            }

            TopicInstance instance = new TopicInstance(market.getName());
            Crown.getTradersGuild().createVote(instance);
        }
    }

    public static class TopicInstance implements VoteTopic {
        private final String shop;

        public TopicInstance(String shop) {
            this.shop = shop;
        }

        public String getShop() {
            return shop;
        }

        public UUID getOwner() {
            return Crown.getMarkets().get(shop).getOwner();
        }

        CrownUser user() {
            return UserManager.getUser(getOwner());
        }

        @Override
        public @Nullable PostVoteTask onVoteEnd(VoteResult result) {
            if(result.isWin()) {
                user().sendOrMail(
                        Component.translatable("guilds.topics.evict.notice.succeed")
                                .append(Component.newline())
                                .append(Component.translatable(
                                        "guilds.topics.evict.notice.succeed2",
                                        NamedTextColor.DARK_RED,
                                        FtcFormatter.millisIntoTime(ComVars.getEvictionCleanupTime())
                                ))
                                .color(NamedTextColor.RED)
                );

                return new PostVoteTask(
                        new Date(System.currentTimeMillis() + ComVars.getEvictionCleanupTime()),
                        new JsonPrimitive(shop),
                        KEY
                );
            }

            user().sendOrMail(
                    Component.translatable("guilds.topics.evict.notice.fail")
                            .color(NamedTextColor.GREEN)
            );
            return null;
        }

        @Override
        public void onVotingBegin() {
            user().sendOrMail(
                    Component.translatable("guilds.topics.evict.notice.begin")
                            .color(NamedTextColor.GRAY)
            );
        }

        @Override
        public EvictTopic getType() {
            return INSTANCE;
        }

        @Override
        public VoteModifier makeModifier(Dynasty dynasty) {
            UUID shopOwner = getOwner();
            Relation r = dynasty.getRelationWith(shopOwner);

            return new VoteModifier(r.getValue() / 2);
        }
    }
}
