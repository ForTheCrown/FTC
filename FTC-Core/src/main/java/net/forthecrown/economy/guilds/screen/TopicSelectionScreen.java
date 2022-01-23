package net.forthecrown.economy.guilds.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.economy.guilds.topics.VoteTopic;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public class TopicSelectionScreen {
    public static BuiltInventory create() {
        // If no topics, no inventory
        if(Registries.VOTE_TOPICS.isEmpty()) return null;

        // One topic, don't bother showing entire inventory
        // Just show that one topic's screen
        if(Registries.VOTE_TOPICS.size() == 1) {
            return createFor(Registries.VOTE_TOPICS.iterator().next());
        }

        InventoryBuilder builder = new InventoryBuilder(findSize())
                .title(Component.text("Select voting topic"))
                .add(new InventoryBorder());

        int index = 0;
        for (VoteTopic t: Registries.VOTE_TOPICS) {
            int x = index % 5;
            int z = index / 5;
            InventoryPos pos = new InventoryPos(x, z);

            builder.add(new TopicSelectionOption(pos, t));

            index++;
        }

        return builder.build();
    }

    private static BuiltInventory createFor(VoteTopic<?> topic) {
        InventoryBuilder builder = new InventoryBuilder(topic.screenSize())
                .title(topic.screenTitle());

        InventoryPos start;
        if(builder.size() == 27 || builder.size() == 36) start = new InventoryPos(1, 1);
        else start = new InventoryPos(2, 2);

        InvPosProvider provider = InvPosProvider.create(builder.size() - 1, start);
        builder.addAll(topic.getClickOptions(provider));

        return builder.build();
    }

    private static int findSize() {
        int voteRows = Registries.VOTE_TOPICS.size() / 5;
        int maxRows = voteRows + 4;

        return maxRows * 9;
    }

    record TopicSelectionOption(InventoryPos pos, VoteTopic topic) implements CordedInventoryOption {
        @Override
        public InventoryPos getPos() {
            return pos;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {
            inventory.setItem(getPos(), topic.createSelectionScreenItem());
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            createFor(topic).open(user);
        }
    }
}
