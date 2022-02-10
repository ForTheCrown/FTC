package net.forthecrown.useables.kits;

import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FtcKitManager extends AbstractJsonSerializer implements KitManager {
    private final Map<Key, FtcKit> kits = new HashMap<>();

    public FtcKitManager(){
        super("kits");

        reload();
        Crown.logger().info("Kits loaded");
    }

    @Override
    protected void save(JsonWrapper json) {
        for (FtcKit k: kits.values()){
            json.add(k.key().asString(), k.serialize());
        }
    }

    @Override
    protected void reload(JsonWrapper json) {
        kits.clear();
        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            try {
                Key key = Keys.parse(e.getKey());
                kits.put(key, new FtcKit(key, e.getValue()));
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public Kit get(Key key) {
        key = FtcUtils.ensureBukkit(key);
        return kits.get(key);
    }

    @Override
    public Kit register(Key key, List<ItemStack> value) {
        key = FtcUtils.ensureBukkit(key);
        FtcKit kit = new FtcKit(key, value);
        kits.put(key, kit);

        return kit;
    }

    @Override
    public Kit remove(Key key) {
        key = FtcUtils.ensureBukkit(key);
        return kits.remove(key);
    }

    @Override
    public Set<Key> keySet() {
        return kits.keySet();
    }

    @Override
    public boolean contains(Key key) {
        key = FtcUtils.ensureBukkit(key);
        return kits.containsKey(key);
    }

    @Override
    public boolean contains(Kit value) {
        return kits.containsValue(value);
    }

    @Override
    public Collection<Kit> values() {
        return new ArrayList<>(kits.values());
    }

    @Override
    public List<Kit> getUsableFor(Player player) {
        List<Kit> useableKits = new ArrayList<>();

        for (FtcKit k: kits.values()){
            if(k.testSilent(player)) useableKits.add(k);
        }

        return useableKits;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestMatching(builder, ListUtils.convert(getUsableFor(context.getSource().asPlayer()), Kit::getName));
    }
}
