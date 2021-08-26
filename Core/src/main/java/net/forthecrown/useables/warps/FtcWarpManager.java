package net.forthecrown.useables.warps;

import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FtcWarpManager extends AbstractJsonSerializer implements WarpManager {
    private final Map<Key, FtcWarp> warps = new HashMap<>();

    public FtcWarpManager(){
        super("warps");

        reload();
        Crown.logger().info("Warps loaded");
    }

    @Override
    protected void save(JsonBuf json) {
        for (Map.Entry<Key, FtcWarp> e: warps.entrySet()){
            json.add(e.getKey().asString(), e.getValue().serialize());
        }
    }

    @Override
    protected void reload(JsonBuf json) {
        warps.clear();
        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            try {
                warps.put(Key.key(e.getKey()), new FtcWarp(e.getValue()));
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public List<Warp> getUsableFor(Player player) {
        List<Warp> result = new ArrayList<>();

        for (FtcWarp w: warps.values()){
            if(w.testSilent(player)) result.add(w);
        }

        return result;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if(context.getSource().isPlayer()){
            Player player = context.getSource().asPlayer();
            String token = builder.getRemaining().toLowerCase();

            for (FtcWarp warp: warps.values()){
                if(!warp.testSilent(player)) continue;
                String name = warp.key().value();

                if(name.toLowerCase().startsWith(token)) builder.suggest(name, ChatUtils.adventureToVanilla(warp.asHoverEvent().value()));
            }

            return builder.buildFuture();
        } else return FtcSuggestionProvider.suggestRegistry(builder, this);
    }

    @Override
    public Warp get(Key key) {
        key = FtcUtils.checkNotBukkit(key);
        return warps.get(key);
    }

    @Override
    public Warp register(Key key, Location value) {
        key = FtcUtils.checkNotBukkit(key);
        FtcWarp warp = new FtcWarp(key, value);
        warps.put(key, warp);
        return warp;
    }

    @Override
    public void remove(Key key) {
        key = FtcUtils.checkNotBukkit(key);
        warps.remove(key);
    }

    @Override
    public Set<Key> keySet() {
        return warps.keySet();
    }

    @Override
    public boolean contains(Key key) {
        key = FtcUtils.checkNotBukkit(key);
        return warps.containsKey(key);
    }

    @Override
    public boolean contains(Warp value) {
        return warps.containsValue(value);
    }

    @Override
    public Collection<Warp> values() {
        return new ArrayList<>(warps.values());
    }
}
