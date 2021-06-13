package net.forthecrown.vikings.valhalla.triggers.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.active.ActiveRaid;
import net.forthecrown.vikings.valhalla.triggers.TriggerAction;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;

public class TrigActionCommand implements TriggerAction<Event> {
    public static Key KEY = Key.key(Vikings.inst, "command");

    private String command;

    @Override
    public void deserialize(JsonElement element) throws CommandSyntaxException {
        this.command = element.getAsString();
    }

    @Override
    public void parse(StringReader reader) throws CommandSyntaxException {
        this.command = reader.getString();
    }

    @Override
    public void trigger(Player player, ActiveRaid raid, Event event) {
        String formatted = command.replaceAll("%p", player.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(command);
    }

    @Override
    public @NonNull Key key() {
        return KEY;
    }
}
