package net.forthecrown.economy.market.guild.topics;

import com.google.gson.JsonElement;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.serializer.SerializerType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface VoteTopicType<T extends VoteTopic> extends SerializerType<T> {
    @Override
    T deserialize(JsonElement element);

    @Override
    JsonElement serialize(T value);

    @Override
    @NotNull Key key();

    Component categoryText();
    Component displayName(T value);

    BuiltInventory getInventory();
    CordedInventoryOption getSelectionOption();
}
