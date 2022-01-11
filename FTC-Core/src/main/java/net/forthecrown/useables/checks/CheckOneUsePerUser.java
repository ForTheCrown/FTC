package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Keys;
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

public class CheckOneUsePerUser implements UsageCheck<CheckOneUsePerUser.CheckInstance> {
    public static final Key KEY = Keys.forthecrown("one_use_per_user");

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

    @Override
    public boolean requiresInput() {
        return false;
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
        public Component failMessage(Player player) {
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
}
