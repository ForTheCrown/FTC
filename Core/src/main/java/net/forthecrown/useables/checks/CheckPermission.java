package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckPermission implements UsageCheck<CheckPermission.CheckInstance> {
    public static final Key KEY = Keys.ftccore("required_permission");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(reader.readUnquotedString());
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(element.getAsString());
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return new JsonPrimitive(value.getPermission());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {

        private final String perm;

        public CheckInstance(String perm){
            this.perm = perm;
        }

        public String getPermission() {
            return perm;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "perm=" + perm + '}';
        }

        @Override
        public Component failMessage(Player player) {
            return Component.text("You don't have permission to use this")
                    .color(NamedTextColor.GRAY);
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            return player.hasPermission(perm);
        }
    }
}
