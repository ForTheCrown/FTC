package net.forthecrown.economy.market.guild.topics;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

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
                            .addLore(Component.text("Can be for any reason").style(nonItalic(NamedTextColor.GRAY)))

                            .build()
            );
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            EvictOwnerTopic.INSTANCE.getInventory().open(user);
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
            CrownUser head = UserManager.getUser(id);

            ItemStackBuilder builder = new ItemStackBuilder(Material.PLAYER_HEAD, 1)
                    .setProfile(head)
                    .setName(head.nickDisplayName().style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)));



            inventory.setItem(
                    pos,
                    builder.build()
            );
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
