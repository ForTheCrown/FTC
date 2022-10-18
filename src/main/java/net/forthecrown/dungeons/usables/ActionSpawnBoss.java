package net.forthecrown.dungeons.usables;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.text.Text;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.useables.*;
import net.forthecrown.grenadier.CommandSource;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionSpawnBoss extends UsageAction {
    // --- TYPE ---
    public static final UsageType<ActionSpawnBoss> TYPE = UsageType.of(ActionSpawnBoss.class)
            .setSuggests(RegistryArguments.DUNGEON_BOSS::listSuggestions);

    private final String bossKey;

    public ActionSpawnBoss(String bossKey) {
        super(TYPE);
        this.bossKey = bossKey;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        Registries.DUNGEON_BOSSES.get(bossKey).ifPresent(boss -> {
            boss.attemptSpawn(player);
        });
    }

    @Override
    public @Nullable Component displayInfo() {
        return Text.format("boss='{0}'", bossKey);
    }

    @Override
    public @Nullable Tag save() {
        return StringTag.valueOf(bossKey);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionSpawnBoss parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionSpawnBoss(RegistryArguments.DUNGEON_BOSS.parse(reader).getKey());
    }

    @UsableConstructor(ConstructType.JSON)
    public static ActionSpawnBoss fromJson(JsonElement element) {
        return new ActionSpawnBoss(element.getAsString());
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionSpawnBoss load(Tag tag) {
        return new ActionSpawnBoss(tag.getAsString());
    }
}