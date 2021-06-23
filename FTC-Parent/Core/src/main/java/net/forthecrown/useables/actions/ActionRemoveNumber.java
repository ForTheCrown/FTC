package net.forthecrown.core.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.useables.UsageAction;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.entity.Player;

public class ActionRemoveNumber implements UsageAction {
    public static final Key BAL_KEY = Key.key(CrownCore.inst(), "remove_balance");
    public static final Key GEM_KEY = Key.key(CrownCore.inst(), "remove_gems");

    private final boolean fromBal;
    private int amount;

    public ActionRemoveNumber(boolean fromBal) {
        this.fromBal = fromBal;
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        amount = json.getAsInt();
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        amount = reader.readInt();
    }

    @Override
    public void onInteract(Player player) {
        if (fromBal) CrownCore.getBalances().add(player.getUniqueId(), -amount);
        else {
            CrownUser user = UserManager.getUser(player);
            user.setGems(user.getGems() - amount);
        }
    }

    @Override
    public Key key() {
        return fromBal ? BAL_KEY : GEM_KEY;
    }

    @Override
    public String asString() {
        return  getClass().getSimpleName() + "{removesfrom=" + (fromBal ? "balance" : "gems") + ",amount=" + amount + "}";
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionRemoveNumber thing = (ActionRemoveNumber) o;

        return new EqualsBuilder()
                .append(fromBal, thing.fromBal)
                .append(amount, thing.amount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(fromBal)
                .append(amount)
                .toHashCode();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isFromBal() {
        return fromBal;
    }
}
