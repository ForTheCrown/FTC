package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.economy.Balances;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CheckNumber implements UsageCheck<CheckNumber.CheckInstance> {
    public static final Key BAL_KEY = Key.key(CrownCore.inst(), "required_balance");
    public static final Key GEM_KEY = Key.key(CrownCore.inst(), "required_gems");

    private final boolean checkBal;

    public CheckNumber(boolean checkBal) {
        this.checkBal = checkBal;
    }

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(checkBal, reader.readInt());
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(checkBal, element.getAsInt());
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return new JsonPrimitive(value.getAmount());
    }

    @Override
    public @NotNull Key key() {
        return checkBal ? BAL_KEY : GEM_KEY;
    }


    public static class CheckInstance implements UsageCheckInstance {

        private final boolean checkBal;
        private final int amount;

        public CheckInstance(boolean checkBal, int amount) {
            this.checkBal = checkBal;
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public boolean checkingBal() {
            return checkBal;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "amount=" + amount + '}';
        }

        @Override
        public Component failMessage() {
            return Component.text("You need at least ")
                .color(NamedTextColor.GRAY)
                .append((checkBal ? Balances.formatted(amount) : ChatFormatter.queryGems(amount)).color(NamedTextColor.GOLD))
                .append(Component.text(" to use this."));
        }

        @Override
        public @NotNull Key typeKey() {
            return checkBal ? BAL_KEY : GEM_KEY;
        }

        @Override
        public boolean test(Player player) {
            if(checkBal) return CrownCore.getBalances().canAfford(player.getUniqueId(), amount);

            return UserManager.getUser(player).getGems() >= amount;
        }

        @Override
        public Consumer<Player> onSuccess() {
            return plr -> {
                if(checkBal) CrownCore.getBalances().add(plr.getUniqueId(), -amount, false);
                else UserManager.getUser(plr).addGems(-amount);
            };
        }
    }
}
