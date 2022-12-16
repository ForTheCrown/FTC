package net.forthecrown.dungeons.usables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionBossInfo extends UsageAction {
    // --- TYPE ---
    public static final UsageType<ActionBossInfo> TYPE = UsageType.of(ActionBossInfo.class)
            .setSuggests(RegistryArguments.DUNGEON_BOSS::listSuggestions);

    private final String bossKey;

    public ActionBossInfo(String bossKey) {
        super(TYPE);
        this.bossKey = bossKey;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        Registries.DUNGEON_BOSSES.get(bossKey).ifPresent(boss -> {
            if (boss.getSpawnRequirement() == null) {
                return;
            }

            player.sendMessage(boss.getSpawnRequirement().denyMessage(player));
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
    public static ActionBossInfo parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionBossInfo(RegistryArguments.DUNGEON_BOSS.parse(reader).getKey());
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionBossInfo load(Tag tag) {
        return new ActionBossInfo(tag.getAsString());
    }
}