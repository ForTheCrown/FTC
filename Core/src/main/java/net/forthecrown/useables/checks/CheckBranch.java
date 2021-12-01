package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.data.Faction;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckBranch implements UsageCheck<CheckBranch.CheckInstance> {
    public static final Key KEY = Keys.ftccore("required_branch");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(FtcCommands.BRANCH.parse(reader));
    }

    @Override
    public CheckInstance deserialize(JsonElement element) {
        return new CheckInstance(JsonUtils.readEnum(Faction.class, element));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return JsonUtils.writeEnum(value.getBranch());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {

        private final Faction faction;

        CheckInstance(Faction check) {
            this.faction = check;
        }

        public Faction getBranch() {
            return faction;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "branch=" + faction.name().toLowerCase() + '}';
        }

        @Override
        public Component failMessage(Player player) {
            return Component.text("You need to be a ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(faction.getSingularName()))
                    .append(Component.text(" to use this."));
        }

        @Override
        public boolean test(Player player) {
            return UserManager.getUser(player).getFaction() == faction;
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }
    }
}
