package net.forthecrown.core.types.interactable.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.types.interactable.InteractionAction;
import net.forthecrown.grenadier.CommandSource;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.entity.Player;

public class ActionRemoveThing implements InteractionAction {
    private final boolean fromBal;
    private int amount;

    public ActionRemoveThing(boolean fromBal) {
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
        if (fromBal) FtcCore.getBalances().add(player.getUniqueId(), -amount);
        else {
            CrownUser user = UserManager.getUser(player);
            user.setGems(user.getGems() - amount);
        }
    }

    @Override
    public String getRegistrationName() {
        return "remove_" + (fromBal ? "balance" : "gems");
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

        ActionRemoveThing thing = (ActionRemoveThing) o;

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
}
