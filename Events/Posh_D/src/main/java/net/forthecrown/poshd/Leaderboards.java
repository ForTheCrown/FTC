package net.forthecrown.poshd;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.crown.ArmorStandLeaderboard;
import net.forthecrown.crown.LeaderboardFormatter;
import net.forthecrown.crown.ObjectiveLeaderboard;
import net.forthecrown.crown.ScoreFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Leaderboards implements Iterable<ObjectiveLeaderboard>, SuggestionProvider<CommandSource> {
    public static final Component DEFAULT_BORDER = Component.text("------=o=O=o------");

    Leaderboards() {}

    public static final LeaderboardFormatter FORMATTER = new LeaderboardFormatter() {
        @Override
        public Component formatName(int pos, String name, Component score) {
            return Component.text()
                    .append(Component.text(pos + ". ").color(NamedTextColor.GOLD))
                    .append(Component.text(name + ": "))
                    .append(score.color(NamedTextColor.YELLOW))
                    .build();
        }
    };

    private final Map<String, ObjectiveLeaderboard> leaderboards = new Object2ObjectOpenHashMap<>();

    public ObjectiveLeaderboard create(Location l, Objective obj, Component... title) {
        ObjectiveLeaderboard leaderboard = new ObjectiveLeaderboard(obj, l, title);
        leaderboard.setBorder(DEFAULT_BORDER);
        leaderboard.setOrder(ArmorStandLeaderboard.Order.LOW_TO_HIGH);
        leaderboard.setFormat(FORMATTER);
        leaderboard.setScoreFormatter(ScoreFormatter.timerFormat());

        add(leaderboard);
        return leaderboard;
    }

    public void add(ObjectiveLeaderboard leaderboard) {
        leaderboards.put(leaderboard.getObjective().getName(), leaderboard);
    }

    public void remove(ObjectiveLeaderboard leaderboard) {
        leaderboards.remove(leaderboard.getObjective().getName());
    }

    public ObjectiveLeaderboard get(Objective objective) {
        return leaderboards.get(objective.getName());
    }

    public void save() {
        File file = getFileSafe();

        if(leaderboards.isEmpty()) {
            file.delete();
            return;
        }

        JsonArray array = new JsonArray();

        for (ObjectiveLeaderboard l: this) {
            JsonObject json = new JsonObject();

            json.addProperty("obj", l.getObjective().getName());
            json.addProperty("order", l.getOrder().name().toLowerCase());
            json.addProperty("size", l.getSize());

            json.add("location", EventUtil.writeLocation(l.getLocation()));

            JsonArray title = new JsonArray();
            for (Component c: l.getTitle()) {
                title.add(EventUtil.writeChat(c));
            }

            json.add("title", title);
            json.add("border", EventUtil.writeChat(l.getBorder()));

            array.add(json);
        }

        try {
            EventUtil.writeFile(array, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        clear();

        File f = getFile();
        if(!f.exists() || f.isDirectory()) return;

        try {
            JsonArray array = EventUtil.readFile(f).getAsJsonArray();

            for (JsonElement e: array) {
                JsonObject json = e.getAsJsonObject();

                Location l = EventUtil.readLocation(json.getAsJsonObject("location"));
                Objective obj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(json.get("obj").getAsString());

                JsonArray titlesJson = json.getAsJsonArray("title");
                Component[] title = new Component[titlesJson.size()];
                for (int i = 0; i < titlesJson.size(); i++) {
                    title[i] = EventUtil.readChat(titlesJson.get(i));
                }

                ObjectiveLeaderboard leaderboard = create(l, obj, title);
                leaderboard.setBorder(EventUtil.readChat(json.get("border")));
                leaderboard.setSize(json.get("size").getAsByte());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        for (ObjectiveLeaderboard l: this)   {
            l.destroy();
        }

        leaderboards.clear();
    }


    public boolean isEmpty() {
        return leaderboards.isEmpty();
    }

    private File getFile() {
        return new File(Main.inst.getDataFolder(), "leaderboards.json");
    }

    private File getFileSafe() {
        File f = getFile();
        if(f.isDirectory()) f.delete();

        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return f;
    }

    @NotNull
    @Override
    public Iterator<ObjectiveLeaderboard> iterator() {
        return leaderboards.values().iterator();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestMatching(builder, leaderboards.keySet());
    }
}
