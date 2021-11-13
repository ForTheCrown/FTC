package net.forthecrown.economy.pirates.merchants;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.commands.clickevent.ClickEventManager;
import net.forthecrown.commands.clickevent.ClickEventTask;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Faction;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.CrownRandom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class GrapplingHookMerchant implements BlackMarketMerchant, ClickEventTask {
    private static final Key KEY = Squire.createPiratesKey("gh_merchant");

    private final String npcID;

    private int price = 50000;
    private byte uses = 75;
    private boolean disabled;

    public GrapplingHookMerchant() {
        this.npcID = ClickEventManager.registerClickEvent(this);
    }

    @Override
    public void onUse(CrownUser user, Entity entity) {
        try {
            checkPreconditions(user);
        } catch (RoyalCommandException e){
            user.sendMessage(e.getComponentMessage());
            return;
        }

        ClickEventManager.allowCommandUsage(user.getPlayer(), true);
        Component button = Component.translatable("buttons.purchase", NamedTextColor.AQUA)
                .hoverEvent(Component.translatable("gh.buy.hover"))
                .clickEvent(ClickEventManager.getClickEvent(npcID));

        user.sendMessage(Component.translatable("gh.buy", NamedTextColor.GRAY, button));
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public void run(Player player, String[] args) throws RoyalCommandException {
        checkPreconditions(UserManager.getUser(player));

        Crown.getEconomy().add(player.getUniqueId(), -price);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gh give " + player.getName() + (uses == -1 ? "" : " " + uses));
    }

    private void checkPreconditions(CrownUser user) throws RoyalCommandException {
        if(!Crown.getEconomy().has(user.getUniqueId(), price)){
            throw FtcExceptionProvider.cannotAfford(price);
        }

        if(user.getFaction() != Faction.PIRATES){
            throw FtcExceptionProvider.notPirate();
        }

        if(disabled) throw FtcExceptionProvider.translatable("gh.disabled", NamedTextColor.GRAY);
    }

    @Override
    public Inventory createInventory(CrownUser user) {
        throw new UnsupportedOperationException("no");
    }

    @Override
    public void load(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        this.price = json.get("price").getAsInt();
        this.uses = json.get("uses").getAsByte();
        this.disabled = json.get("disabled").getAsBoolean();
    }

    @Override
    public void update(CrownRandom random, byte day) {
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("price", price);
        json.addProperty("uses", uses);
        json.addProperty("disabled", disabled);

        return json;
    }
}
