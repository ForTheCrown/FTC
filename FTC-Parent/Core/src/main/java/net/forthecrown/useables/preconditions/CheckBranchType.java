package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckBranchType implements UsageCheck<CheckBranchType.CheckInstance> {
    public static final Key KEY = CrownCore.coreKey("required_branch");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(CoreCommands.BRANCH.parse(reader));
    }

    @Override
    public CheckInstance deserialize(JsonElement element) {
        return new CheckInstance(JsonUtils.readEnum(Branch.class, element));
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

        private final Branch branch;

        CheckInstance(Branch check) {
            this.branch = check;
        }

        public Branch getBranch() {
            return branch;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "branch=" + branch.name().toLowerCase() + '}';
        }

        @Override
        public Component failMessage() {
            return Component.text("You need to be a ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(branch.getSingularName()))
                    .append(Component.text(" to use this."));
        }

        @Override
        public boolean test(Player player) {
            return UserManager.getUser(player).getBranch() == branch;
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }
    }
}
