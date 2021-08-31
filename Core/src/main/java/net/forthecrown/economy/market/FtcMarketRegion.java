package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.Balances;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class FtcMarketRegion extends AbstractJsonSerializer implements MarketRegion {
    private final Object2ObjectMap<UUID, FtcMarketShop> byOwner = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, FtcMarketShop> byName = new Object2ObjectOpenHashMap<>();

    public FtcMarketRegion() {
        super("market_shops");

        reload();
        Crown.logger().info("Market shops loaded");
    }

    @Override
    protected void save(JsonBuf json) {
        for (FtcMarketShop s: byName.values()) {
            json.add(s.getWorldGuardRegion().getId(), s);
        }
    }

    @Override
    protected void reload(JsonBuf json) {
        clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            add(new FtcMarketShop(e.getValue(), e.getKey()));
        }
    }

    @Override
    public FtcMarketShop get(UUID owner) {
        return byOwner.get(owner);
    }

    @Override
    public FtcMarketShop get(String claimName) {
        return byName.get(claimName);
    }

    @Override
    public void add(FtcMarketShop claim) {
        byName.put(claim.getWorldGuardRegion().getId(), claim);
        if(claim.getOwner() != null) byOwner.put(claim.getOwner(), claim);
    }

    @Override
    public void attemptPurchase(FtcMarketShop claim, Player player) {
        CrownUser user = UserManager.getUser(player);
        Balances balances = Crown.getBalances();

        try {
            claim.testCanPurchase(user, balances, this);
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(player, e);
            return;
        }

        claim.purchase(user, balances);
    }

    @Override
    public void eject(UUID owner) {
        FtcMarketShop shop = get(owner);
        Validate.notNull(shop, "Given UUID does not own any market shops");

        shop.unclaim();
        byOwner.remove(owner);
    }

    @Override
    public void remove(String name) {
        FtcMarketShop shop = get(name);

        byName.remove(name);
        if(shop.getOwner() != null) byOwner.remove(shop.getOwner());
    }

    @Override
    public void clear() {
        byOwner.clear();
        byName.clear();
    }

    @Override
    public int size() {
        return byName.size();
    }
}
