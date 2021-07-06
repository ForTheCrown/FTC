package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CheckNeverUsed implements UsageCheck<CheckNeverUsed.CheckInstance> {
    public static final Key KEY = Key.key(CrownCore.inst(), "never_used");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(false);
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(element.getAsBoolean());
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return new JsonPrimitive(value.isUsed());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {
        private boolean used;

        CheckInstance(boolean used) {
            this.used = used;
        }

        public boolean isUsed() {
            return used;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "used=" + used + '}';
        }

        @Override
        public Component failMessage() {
            return Component.text("Only one person may ever use this")
                    .color(NamedTextColor.GRAY);
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            return !used;
        }

        @Override
        public Consumer<Player> onSuccess() {
            return plr -> used = true;
        }
    }
}
