package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionAddNumber implements UsageAction<ActionAddNumber.ActionInstance> {
    public static final NamespacedKey
            GEM_KEY = Keys.forthecrown("add_gems"),
            BAL_KEY = Keys.forthecrown("add_bal");

    private final boolean toBal;

    public ActionAddNumber(boolean toBal) {
        this.toBal = toBal;
    }

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        int amount = reader.readInt();

        return new ActionInstance(toBal, amount);
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        int amount = json.getInt("amount");

        return new ActionInstance(toBal, amount);
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        JsonWrapper json = JsonWrapper.empty();

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

        public ActionInstance(boolean toBal, int amount) {
            this.toBal = toBal;
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isToBal() {
            return toBal;
        }

        @Override
        public void onInteract(Player player) {
            if(toBal) Crown.getEconomy().add(player.getUniqueId(), amount);
            else UserManager.getUser(player).addGems(amount);
        }

        @Override
        public String asString() {
            return typeKey().asString() +
                    '{' +
                    "amount=" + amount +
                    '}';
        }

        @Override
        public @NotNull Key typeKey() {
            return toBal ? BAL_KEY : GEM_KEY;
        }
    }
}
