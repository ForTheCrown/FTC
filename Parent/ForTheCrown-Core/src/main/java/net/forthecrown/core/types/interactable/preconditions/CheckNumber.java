package net.forthecrown.core.types.interactable.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.types.interactable.InteractionCheck;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class CheckNumber implements InteractionCheck {
    private final boolean checkBal;
    private int amount;

    public CheckNumber(boolean checkBal) { this.checkBal = checkBal; }

    @Override
    public void parse(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        amount = reader.readInt();
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
        return getClass().getSimpleName() + "{" + "checks=" + (checkBal ? "balance" : "gems") + ",amount=" + amount + "}";
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
