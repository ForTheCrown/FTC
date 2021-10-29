package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.data.Rank;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckRankType implements UsageCheck<CheckRankType.CheckInstance> {
    public static final Key KEY = Crown.coreKey("required_rank");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(FtcCommands.RANK.parse(reader));
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(JsonUtils.readEnum(Rank.class, element));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return JsonUtils.writeEnum(value.getRank());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {

        private final Rank rank;

        CheckInstance(Rank rank) {
            this.rank = rank;
        }

        public Rank getRank() {
            return rank;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "rank=" + rank.name().toLowerCase() + '}';
        }

        @Override
        public Component failMessage(Player player) {
            return Component.text()
                    .color(NamedTextColor.GRAY)
                    .append(Component.text("You need the "))
                    .append(getRank().prefix())
                    .append(Component.text("rank."))
                    .build();
        }

        @Override
        public boolean test(Player player) {
            return UserManager.getUser(player).hasRank(rank);
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }
    }
}
