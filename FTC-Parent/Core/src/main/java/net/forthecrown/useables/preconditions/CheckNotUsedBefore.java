package net.forthecrown.useables.preconditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.useables.UsageCheck;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CheckNotUsedBefore implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.inst(), "not_used_before");

    private final List<UUID> list = new ArrayList<>();

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
    }
}
