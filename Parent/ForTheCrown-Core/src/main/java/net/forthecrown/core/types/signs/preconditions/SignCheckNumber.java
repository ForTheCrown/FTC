package net.forthecrown.core.types.signs.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.types.signs.SignPrecondition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class SignCheckNumber implements SignPrecondition {
    private final boolean checkBal;
    private int amount;

    public SignCheckNumber(boolean checkBal) { this.checkBal = checkBal; }

    @Override
    public void parse(String input) throws CommandSyntaxException {
        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw FtcExceptionProvider.create("Couldn't parse integer: " + input);
        }
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        amount = json.getAsInt();
    }

    @Override
    public String getRegistrationName() {
        return "required_" + (checkBal ? "balance" : "gems");
    }

    @Override
    public String asString() {
        return "SignCheckNumber{" + "checks=" + (checkBal ? "balance" : "gems") + ",amount=" + amount + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You need at least ")
                .color(NamedTextColor.GRAY)
                .append((checkBal ? Balances.formatted(amount) : Component.text(amount + " Gem" + (amount == 1 ? "" : "s"))).color(NamedTextColor.GOLD))
                .append(Component.text(" to use this sign."));
    }

    @Override
    public boolean test(Player player) {
        if (checkBal) return FtcCore.getBalances().canAfford(player.getUniqueId(), amount);

        return UserManager.getUser(player).getGems() >= amount;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(amount);
    }
}
