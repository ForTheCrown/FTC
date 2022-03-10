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
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionShowBossInfo implements UsageAction<ActionShowBossInfo.ActionInstance> {
    public static final Key KEY = Squire.createRoyalKey("show_boss_info");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(RegistryArguments.dungeonBoss().parse(reader));
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(Registries.DUNGEON_BOSSES.read(element));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return value.boss.serialize();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return FtcSuggestionProvider.suggestRegistry(builder, Registries.DUNGEON_BOSSES);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public record ActionInstance(KeyedBoss boss) implements UsageActionInstance {

        @Override
        public String asString() {
            return typeKey().asString() + "{boss=" + boss.key().asString() + "}";
        }

        @Override
        public Key typeKey() {
            return KEY;
        }

        @Override
        public void onInteract(Player player) {
            if(boss.getSpawnRequirement() == null) {
                return;
            }

            player.sendMessage(boss.getSpawnRequirement().denyMessage(player));
        }
    }
}
