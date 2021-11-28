package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionAddNumber implements UsageAction<ActionAddNumber.ActionInstance> {
    public static final Key GEM_KEY = Keys.ftc("add_gems");
    public static final Key BAL_KEY = Keys.ftc("add_bal");

    private final boolean toBal;

    public ActionAddNumber(boolean toBal) {
        this.toBal = toBal;
    }

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        int amount = reader.readInt();

        if(!reader.canRead()) return new ActionInstance(toBal, amount, false);

        reader.skipWhitespace();

        boolean taxed = reader.readBoolean();
        return new ActionInstance(toBal, amount, taxed);
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        int amount = json.getInt("amount");
        boolean taxed = json.getBool("taxed", false);

        return new ActionInstance(toBal, amount, taxed);
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        JsonWrapper json = JsonWrapper.empty();

        json.add("taxed", value.isTaxed());
        json.add("amount", value.getAmount());

        return json.getSource();
    }

    @Override
    public @NotNull Key key() {
        return toBal ? BAL_KEY : GEM_KEY;
    }

    public static class ActionInstance implements UsageActionInstance {
        private final boolean toBal;

        private final int amount;
        private final boolean taxed;

        public ActionInstance(boolean toBal, int amount, boolean taxed) {
            this.toBal = toBal;
            this.amount = amount;
            this.taxed = taxed;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isTaxed() {
            return taxed;
        }

        public boolean isToBal() {
            return toBal;
        }

        @Override
        public void onInteract(Player player) {
            if(toBal) Crown.getEconomy().add(player.getUniqueId(), amount, taxed);
            else UserManager.getUser(player).addGems(amount);
        }

        @Override
        public String asString() {
            return typeKey().asString() +
                    '{' +
                    "amount=" + amount +
                    (toBal ? ",taxed=" + taxed : "") +
                    '}';
        }

        @Override
        public @NotNull Key typeKey() {
            return toBal ? BAL_KEY : GEM_KEY;
        }
    }
}
