package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CheckNotUsedBefore implements UsageCheck<CheckNotUsedBefore.CheckInstance> {
    public static final Key KEY = Key.key(ForTheCrown.inst(), "not_used_before");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(new ArrayList<>());
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(ListUtils.fromIterable(element.getAsJsonArray(), JsonUtils::readUUID));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return JsonUtils.writeCollection(value.getUsed(), JsonUtils::writeUUID);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {
        private final List<UUID> used;

        public CheckInstance(List<UUID> used) {
            this.used = used;
        }

        public List<UUID> getUsed() {
            return used;
        }

        @Override
        public String asString() {
            return typeKey().asString();
        }

        @Override
        public Component failMessage() {
            return Component.text("You can only use this once")
                    .color(NamedTextColor.GRAY);
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            return !used.contains(player.getUniqueId());
        }

        @Override
        public Consumer<Player> onSuccess() {
            return plr -> used.add(plr.getUniqueId());
        }
    }

    /*private final List<UUID> list = new ArrayList<>();

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException { }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        JsonArray array = json.getAsJsonArray();

        list.clear();
        for (JsonElement j: array){
            list.add(UUID.fromString(j.getAsString()));
        }
    }

    @Override
    public Key key() {
        return KEY;
    }

    @Override
    public String asString() {
        return key().asString();
    }

    @Override
    public Component failMessage() {
        return Component.text("You have already used this").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return !list.contains(player.getUniqueId());
    }

    @Override
    public Consumer<Player> onSuccess() {
        return plr -> list.add(plr.getUniqueId());
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeCollection(list, id -> new JsonPrimitive(id.toString()));
    }

    public List<UUID> getList() {
        return list;
    }*/
}
