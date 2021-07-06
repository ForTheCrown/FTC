package net.forthecrown.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.useables.kits.FtcKit;
import net.forthecrown.useables.kits.Kit;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CrownKitRegistry extends AbstractJsonSerializer implements KitRegistry {
    private final Map<Key, FtcKit> kits = new HashMap<>();

    public CrownKitRegistry(){
        super("kits");

        reload();
        CrownCore.logger().info("Kits loaded");
    }

    @Override
    protected void save(JsonObject json) {
        for (FtcKit k: kits.values()){
            json.add(k.key().asString(), k.serialize());
        }
    }

    @Override
    protected void reload(JsonObject json) {
        kits.clear();
        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            try {
                Key key = Key.key(e.getKey());
                kits.put(key, new FtcKit(key, e.getValue()));
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public Kit get(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        return kits.get(key);
    }

    @Override
    public Kit register(Key key, List<ItemStack> raw) {
        key = CrownUtils.checkNotBukkit(key);
        FtcKit kit = new FtcKit(key, raw);
        kits.put(key, kit);

        return kit;
    }

    @Override
    public void remove(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        kits.remove(key);
    }

    @Override
    public Set<Key> keySet() {
        return kits.keySet();
    }

    @Override
    public boolean contains(Key key) {
        key = CrownUtils.checkNotBukkit(key);
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
    protected JsonObject createDefaults(JsonObject json) {
        return json;
    }

    @Override
    public List<Kit> getUseableFor(Player player) {
        List<Kit> useableKits = new ArrayList<>();

        for (FtcKit k: kits.values()){
            if(k.testSilent(player)) useableKits.add(k);
        }

        return useableKits;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestMatching(builder, ListUtils.convert(getUseableFor(context.getSource().asPlayer()), Kit::getName));
    }
}
