package net.forthecrown.core.challenge;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.forthecrown.economy.sell.MenuReader;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;
import net.kyori.adventure.text.Component;

public class ItemChallengeParser {
    public static final String
            KEY_REWARD = "reward",
            KEY_SLOT = "slot",
            KEY_DESCRIPTION = "description",
            KEY_TYPE = "type";

    public static DataResult<ItemChallenge> parse(JsonObject element) {
        JsonWrapper json = JsonWrapper.wrap(element);

        if (!json.has(KEY_SLOT)) {
            return Results.errorResult("Missing %s value", KEY_SLOT);
        }

        Slot slot = MenuReader.readSlot(json.get(KEY_SLOT));
        Reward reward = Reward.EMPTY;

        ResetInterval interval = json.getEnum(
                KEY_TYPE,
                ResetInterval.class,
                ResetInterval.DAILY
        );

        ImmutableList.Builder<Component> desc = ImmutableList.builder();

        for (var c: json.getList(KEY_DESCRIPTION, JsonUtils::readText)) {
            desc.add(c);
        }

        if (json.has(KEY_REWARD)) {
            reward = Reward.deserialize(json.get(KEY_REWARD));
        }

        return DataResult.success(
                new ItemChallenge(slot, reward, desc.build(), interval)
        );
    }
}