package net.forthecrown.dungeons.usables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.checks.UsageCheck;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserDataContainer;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
            CrownUser user = UserManager.getUser(player);
            UserDataContainer container = user.getDataContainer();

            return Bosses.getAccessor().getStatus(container, boss);
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
