package net.forthecrown.economy.market.guild.topics;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EvictOwnerTopic implements VoteTopicType<EvictOwnerTopic.TopicInstance> {
    public static final Key KEY = Crown.coreKey("evict_owner");
    public static final EvictOwnerTopic INSTANCE = new EvictOwnerTopic();

    @Override
    public TopicInstance deserialize(JsonElement element) {
        return new TopicInstance(JsonUtils.readUUID(element));
    }

    @Override
    public JsonElement serialize(TopicInstance value) {
        return JsonUtils.writeUUID(value.getOwner());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public Component categoryText() {
        return null;
    }

    @Override
    public Component displayName(TopicInstance value) {
        return null;
    }

    @Override
    public BuiltInventory getInventory() {
        return null;
    }

    @Override
    public CordedInventoryOption getSelectionOption() {
        return EvictOwnerOption.INSTANCE;
    }

    public static class EvictOwnerOption implements CordedInventoryOption {
        public static final EvictOwnerOption INSTANCE = new EvictOwnerOption();

        @Override
        public InventoryPos getPos() {
            return null;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {

        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {

        }
    }

    private static class EvictHeadOption implements CordedInventoryOption {
        private final InventoryPos pos;
        private final UUID id;

        private EvictHeadOption(InventoryPos pos, UUID id) {
            this.pos = pos;
            this.id = id;
        }

        public UUID getId() {
            return id;
        }

        @Override
        public InventoryPos getPos() {
            return pos;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {

        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {

        }
    }

    public static class TopicInstance implements VoteTopic {
        private final UUID owner;

        public TopicInstance(UUID owner) {
            this.owner = owner;
        }

        public UUID getOwner() {
            return owner;
        }

        @Override
        public void onVoteSucceed() {

        }

        @Override
        public void onVoteFail() {

        }

        @Override
        public void onVotingBegin() {

        }

        @Override
        public EvictOwnerTopic getType() {
            return INSTANCE;
        }
    }
}
