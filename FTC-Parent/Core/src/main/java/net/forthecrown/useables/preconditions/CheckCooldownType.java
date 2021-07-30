package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.serializer.JsonBuf;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CheckCooldownType implements UsageCheck<CheckCooldownType.CheckInstance> {
    public static final Key KEY = ForTheCrown.coreKey("cooldown");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(TimeArgument.time().parse(reader) * 50L);
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        long duration = json.getLong("duration");
        Map<UUID, Long> onCooldown = json.getMap("onCooldown", UUID::fromString, JsonElement::getAsLong);

        return new CheckInstance(onCooldown, duration);
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        JsonBuf json = JsonBuf.empty();

        json.add("duration", value.getDuration());
        json.addMap("onCooldown", value.getOnCooldown(), UUID::toString, JsonPrimitive::new);

        return json.getSource();
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return TimeArgument.time().listSuggestions(context, builder);
    }

    public static class CheckInstance implements UsageCheckInstance {
        private final Map<UUID, Long> onCooldown;
        private final long duration;

        CheckInstance(long time) {
            this.duration = time;
            this.onCooldown = new HashMap<>();
        }

        CheckInstance(Map<UUID, Long> onCooldown, long time) {
            this.onCooldown = onCooldown;
            this.duration = time;
        }

        public long getDuration() {
            return duration;
        }

        public Map<UUID, Long> getOnCooldown() {
            return onCooldown;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "duration=" + duration + '}';
        }

        @Override
        public Component failMessage() {
            return null;
        }

        @Override
        public Component personalizedMessage(Player player) {
            return duration > (2400*50) ? Component.text("You cannot use this for ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(ChatFormatter.convertMillisIntoTime(onCooldown.get(player.getUniqueId()) - System.currentTimeMillis())).color(NamedTextColor.GOLD))
                    : null;
        }

        @Override
        public Consumer<Player> onSuccess() {
            return  plr -> onCooldown.put(plr.getUniqueId(), System.currentTimeMillis() + duration);
        }

        @Override
        public boolean test(Player player) {
            if(!onCooldown.containsKey(player.getUniqueId())) return true;

            long until = onCooldown.get(player.getUniqueId());

            if(System.currentTimeMillis() > until){
                onCooldown.remove(player.getUniqueId());
                return true;
            }
            return false;
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }
    }
}
