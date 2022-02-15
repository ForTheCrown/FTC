package net.forthecrown.dungeons.usables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionSpawnBoss implements UsageAction<ActionSpawnBoss.ActionInstance> {
    public static final Key KEY = Squire.createRoyalKey("spawn_boss");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(RegistryArguments.dungeonBoss().parse(reader));
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        Key key = JsonUtils.readKey(element);

        return new ActionInstance(Registries.DUNGEON_BOSSES.get(key));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return value.getBoss().serialize();
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return FtcSuggestionProvider.suggestRegistry(builder, Registries.DUNGEON_BOSSES);
    }

    public static class ActionInstance implements UsageActionInstance {
        private final KeyedBoss boss;

        public ActionInstance(KeyedBoss boss) {
            this.boss = boss;
        }

        public KeyedBoss getBoss() {
            return boss;
        }

        @Override
        public void onInteract(Player player) {
            boss.attemptSpawn(player);
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "boss=" + boss.key().asString() + '}';
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }
    }
}
