package net.forthecrown.august.usables;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import net.forthecrown.august.EventUtil;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.preconditions.UsageCheck;
import net.forthecrown.useables.preconditions.UsageCheckInstance;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CheckCanEnter implements UsageCheck<CheckCanEnter.CheckInstance> {
    private static final CheckInstance INSTANCE = new CheckInstance();
    public static final Key KEY = EventUtil.createEventKey("can_enter_event");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) {
        return INSTANCE;
    }

    @Override
    public CheckInstance deserialize(JsonElement element) {
        return INSTANCE;
    }

    @Override
    public JsonElement serialize(CheckInstance instance) {
        return JsonNull.INSTANCE;
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {

        @Override
        public @Nullable Component failMessage() {
            return null;
        }

        @Override
        public boolean test(Player player) {
            return EventUtil.canEnter(player);
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{}";
        }

        @Override
        public Key typeKey() {
            return KEY;
        }
    }
}
