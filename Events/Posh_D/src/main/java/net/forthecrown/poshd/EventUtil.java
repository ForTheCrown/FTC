package net.forthecrown.poshd;

import com.google.common.base.Charsets;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.forthecrown.crown.EventTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Score;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public final class EventUtil {
    private EventUtil() {}

    static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    //Writes json to a file
    public static void writeFile(JsonElement json, File f) throws IOException {
        FileWriter writer = new FileWriter(f, Charsets.UTF_8);

        JsonWriter jWriter = gson.newJsonWriter(writer);
        gson.toJson(json, jWriter);

        jWriter.close();
        writer.close();
    }

    public static JsonElement readFile(File file) throws IOException {
        FileReader reader = new FileReader(file, Charsets.UTF_8);
        JsonElement json = JsonParser.parseReader(reader);

        reader.close();

        return json;
    }

    public static JsonObject writeLocation(Location location){
        JsonObject result = new JsonObject();

        if(location.getWorld() != null) result.addProperty("world", location.getWorld().getName());

        result.addProperty("x", location.getX());
        result.addProperty("y", location.getY());
        result.addProperty("z", location.getZ());

        if(location.getPitch() != 0f) result.addProperty("pitch", location.getPitch());
        if(location.getYaw() != 0f) result.addProperty("yaw", location.getYaw());

        return result;
    }

    public static Location readLocation(JsonObject json) {
        World world = Objects.requireNonNull(Bukkit.getWorld(json.get("world").getAsString()));

        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();

        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0f;
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0f;

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static JsonElement writeChat(Component text) {
        return GsonComponentSerializer.gson().serializeToTree(text);
    }

    public static Component readChat(JsonElement element) {
        return GsonComponentSerializer.gson().deserializeFromTree(element);
    }

    public static Component stringToNonItalic(String str) {
        return Component.text()
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(str))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .build();
    }

    public static EventTimer createTimer(Player player, Consumer<Player> playerConsumer) {
        EventTimer result = new EventTimer(player, Messages.timerFormatter(), playerConsumer);
        Main.TIMERS.put(player.getUniqueId(), result);

        return result;
    }

    public static boolean isBetterScore(Score record, long score) {
        if(!record.isScoreSet()) return true;
        int recordInt = record.getScore();

        return recordInt > score;
    }

    public static void leave(Player player, Location exitLocation) {
        clearEffects(player);
        leaveTeams(player);
        player.teleport(exitLocation);
    }

    public static void clearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public static void leaveTeams(Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "team leave " + player.getName());
    }
}