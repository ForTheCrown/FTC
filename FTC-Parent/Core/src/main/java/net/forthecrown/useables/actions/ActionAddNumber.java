package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionAddNumber implements UsageAction {
    public static final Key GEM_KEY = CrownCore.coreKey("add_gems");
    public static final Key BAL_KEY = CrownCore.coreKey("add_bal");

    private final boolean toBal;

    private int amount;
    private boolean taxed;

    public ActionAddNumber(boolean toBal) {
        this.toBal = toBal;
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        JsonObject obj = json.getAsJsonObject();

        this.amount = obj.get("amount").getAsInt();
        this.taxed = obj.get("taxed").getAsBoolean();
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        this.amount = reader.readInt();

        if(!reader.canRead()){
            taxed = false;
            return;
        }

        reader.skipWhitespace();

        this.taxed = reader.readBoolean();
    }

    @Override
    public void onInteract(Player player) {
        if(toBal) CrownCore.getBalances().add(player.getUniqueId(), amount, taxed);
        else UserManager.getUser(player).addGems(amount);
    }

    @Override
    public String asString() {
        return key().asString() +
                '{' +
                "amount=" + amount +
                (toBal ? ",taxed=" + taxed : "") +
                '}';
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("amount", amount);
        json.addProperty("taxed", taxed);

        return json;
    }

    @Override
    public @NotNull Key key() {
        return toBal ? BAL_KEY : GEM_KEY;
    }
}
