package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionRemoveNumber implements UsageAction<ActionRemoveNumber.ActionInstance> {
    public static final Key BAL_KEY = Key.key(ForTheCrown.inst(), "remove_balance");
    public static final Key GEM_KEY = Key.key(ForTheCrown.inst(), "remove_gems");

    private final boolean fromBal;

    public ActionRemoveNumber(boolean fromBal) {
        this.fromBal = fromBal;
    }

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(fromBal, reader.readInt());
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(fromBal, element.getAsInt());
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return new JsonPrimitive(value.getAmount());
    }

    @Override
    public @NotNull Key key() {
        return fromBal ? BAL_KEY : GEM_KEY;
    }

    public static class ActionInstance implements UsageActionInstance {
        private final boolean fromBal;
        private final int amount;

        public ActionInstance(boolean fromBal, int amount) {
            this.fromBal = fromBal;
            this.amount = amount;
        }

        @Override
        public void onInteract(Player player) {
            if (fromBal) ForTheCrown.getBalances().add(player.getUniqueId(), -amount);
            else {
                CrownUser user = UserManager.getUser(player);
                user.setGems(user.getGems() - amount);
            }
        }

        @Override
        public Key typeKey() {
            return fromBal ? BAL_KEY : GEM_KEY;
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{amount=" + amount + "}";
        }

        public int getAmount() {
            return amount;
        }

        public boolean isFromBal() {
            return fromBal;
        }
    }
}
