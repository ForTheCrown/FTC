package net.forthecrown.dungeons.usables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.dungeons.boss.DrawnedBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionGiveArtifact implements UsageAction<ActionGiveArtifact.ActionInstance> {
    public static final Key KEY = Squire.createRoyalKey("give_artifact");
    public static final EnumArgument<DrawnedBoss.Artifacts> ARTIFACTS_PARSER = EnumArgument.of(DrawnedBoss.Artifacts.class);

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(ARTIFACTS_PARSER.parse(reader));
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(JsonUtils.readEnum(DrawnedBoss.Artifacts.class, element));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return JsonUtils.writeEnum(value.getArtifact());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return ARTIFACTS_PARSER.listSuggestions(context, builder);
    }

    public static class ActionInstance implements UsageActionInstance {
        private final DrawnedBoss.Artifacts artifact;

        public ActionInstance(DrawnedBoss.Artifacts artifact) {
            this.artifact = artifact;
        }

        public DrawnedBoss.Artifacts getArtifact() {
            return artifact;
        }

        @Override
        public void onInteract(Player player) {
            try {
                player.getInventory().addItem(artifact.item());
            } catch (Exception e){
                player.getWorld().dropItem(player.getLocation(), artifact.item());
            }

            player.sendMessage(
                    Component.text("You got the ")
                            .color(NamedTextColor.GRAY)
                            .append(artifact.item().displayName().color(NamedTextColor.YELLOW))
            );
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "artifact=" + artifact.name().toLowerCase() + '}';
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }
    }
}
