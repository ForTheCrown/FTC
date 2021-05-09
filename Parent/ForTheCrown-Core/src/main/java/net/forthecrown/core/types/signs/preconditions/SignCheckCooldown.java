package net.forthecrown.core.types.signs.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.types.signs.SignPrecondition;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.MapUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignCheckCooldown implements SignPrecondition {
    private final Map<UUID, Long> onCooldown = new HashMap<>();
    private int tickDuration;

    @Override
    public void parse(String input) throws CommandSyntaxException {
        try {
            tickDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw FtcExceptionProvider.create("Couldn't parse integer: " + input);
        }
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
    public String getRegistrationName() {
        return "cooldown";
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
        return tickDuration > 12000 ? Component.text("You cannot use this sign for ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(CrownUtils.convertMillisIntoTime(onCooldown.get(player.getUniqueId()) - System.currentTimeMillis())).color(NamedTextColor.GOLD))
                : null;
    }

    @Override
    public boolean test(Player player) {
        if(!onCooldown.containsKey(player.getUniqueId())){
            onCooldown.put(player.getUniqueId(), System.currentTimeMillis() + (tickDuration * 50));
            return true;
        }

        long until = onCooldown.get(player.getUniqueId());
        if(System.currentTimeMillis() > until) onCooldown.remove(player.getUniqueId());
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
}
