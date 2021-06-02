package net.forthecrown.emperor.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.MapUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class CheckCooldown implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.getNamespace(), "cooldown");

    private final Map<UUID, Long> onCooldown = new HashMap<>();
    private int tickDuration;

    @Override
    public void parse(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        tickDuration = reader.readInt();
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        JsonObject j = json.getAsJsonObject();

        tickDuration = j.get("duration").getAsInt();

        JsonObject array = j.getAsJsonObject("onCooldown");
        onCooldown.clear();
        for (Map.Entry<String, JsonElement> e: array.entrySet()){
            onCooldown.put(UUID.fromString(e.getKey()), e.getValue().getAsLong());
        }
    }

    @Override
    public Key key() {
        return KEY;
    }

    @Override
    public String asString() {
        return "SignCheckCooldown{" + "duration=" + tickDuration + "}";
    }

    @Override
    public Component getFailMessage() {
        return null;
    }

    @Override
    public Component getPersonalizedFailMessage(Player player) {
        return tickDuration > 2400 ? Component.text("You cannot use this for ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(ChatFormatter.convertMillisIntoTime(onCooldown.get(player.getUniqueId()) - System.currentTimeMillis())).color(NamedTextColor.GOLD))
                : null;
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
    public JsonElement serialize() {
        JsonObject result = new JsonObject();

        result.add("duration", new JsonPrimitive(tickDuration));

        JsonObject array = new JsonObject();

        if(!MapUtils.isNullOrEmpty(onCooldown)){
            for (Map.Entry<UUID, Long> e: onCooldown.entrySet()){
                array.add(e.getKey().toString(), new JsonPrimitive(e.getValue()));
            }
        }

        result.add("onCooldown", array);
        return result;
    }

    @Override
    public Consumer<Player> onSuccess() {
        return plr -> onCooldown.put(plr.getUniqueId(), (tickDuration * 50) + System.currentTimeMillis());
    }

    public int getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
    }

    public Map<UUID, Long> getOnCooldown() {
        return onCooldown;
    }
}
