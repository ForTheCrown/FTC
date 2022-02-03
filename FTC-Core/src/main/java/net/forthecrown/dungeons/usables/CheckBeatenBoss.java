package net.forthecrown.dungeons.usables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.checks.UsageCheck;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckBeatenBoss implements UsageCheck<CheckBeatenBoss.CheckInstance> {
    public static final Key KEY = Squire.createRoyalKey("beaten_boss");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(RegistryArguments.dungeonBoss().parse(reader));
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(Registries.DUNGEON_BOSSES.get(JsonUtils.readKey(element)));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return value.boss().serialize();
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public record CheckInstance(DungeonBoss boss) implements UsageCheckInstance {
        @Override
        public boolean test(Player player) {
            Advancement advancement = Bukkit.getAdvancement(boss.advancementKey());
            if(advancement == null) {
                Crown.logger().warn("Cannot check if " + boss.key() + " is beaten, no advancement");
                return false;
            }

            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            return progress.isDone();
        }

        @Override
        public @NotNull Component failMessage(Player player) {
            return Component.translatable("dungeons.cannotEnter", Component.text(boss.getName()));
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{" + boss.key() + "}";
        }

        @Override
        public Key typeKey() {
            return KEY;
        }
    }
}
