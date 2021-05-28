package net.forthecrown.emperor.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class CheckNumber implements UsageCheck {
    public static final Key BAL_KEY = Key.key(CrownCore.getNamespace(), "required_balance");
    public static final Key GEM_KEY = Key.key(CrownCore.getNamespace(), "required_gems");

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
    public Key key() {
        return checkBal ? BAL_KEY : GEM_KEY;
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{" + "checks=" + (checkBal ? "balance" : "gems") + ",amount=" + amount + "}";
    }

    @Override
    public Consumer<Player> onSuccess() {
        return plr -> {
            if(checkBal) CrownCore.getBalances().add(plr.getUniqueId(), -amount, false);
            else UserManager.getUser(plr).addGems(-amount);
        };
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You need at least ")
                .color(NamedTextColor.GRAY)
                .append((checkBal ? Balances.formatted(amount) : Component.text(amount + " Gem" + (amount == 1 ? "" : "s"))).color(NamedTextColor.GOLD))
                .append(Component.text(" to use this."));
    }

    @Override
    public boolean test(Player player) {
        if (checkBal) return CrownCore.getBalances().canAfford(player.getUniqueId(), amount);

        return UserManager.getUser(player).getGems() >= amount;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(amount);
    }

    public int getAmount() {
        return amount;
    }

    public boolean fromBal() {
        return checkBal;
    }
}
