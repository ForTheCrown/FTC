package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.economy.Balances;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.math.BoundingBoxes;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

public class FtcMarketShop implements JsonSerializable {
    private final ProtectedRegion wgRegion;
    private final ShopEntrance[] doors;
    private final World world;
    private final BoundingBox exampleArea;
    private final Vector3i realMin;

    private int price;
    private UUID owner;

    public FtcMarketShop(ProtectedRegion wgRegion, ShopEntrance[] doors, World world, BoundingBox example, Vector3i min, int price) {
        this.wgRegion = wgRegion;
        this.doors = doors;
        this.world = world;
        this.exampleArea = example;
        realMin = min;
        this.price = price;
    }

    public FtcMarketShop(JsonElement element, String regionName) {
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        this.world = Bukkit.getWorld(json.getString("world"));
        this.realMin = Vector3i.of(json.get("realMin"));
        this.doors = json.getArray("doors", ShopEntrance::fromJson, ShopEntrance[]::new);
        this.owner = json.getUUID("owner");
        this.price = json.getInt("price");
        this.exampleArea = json.get("exampleArea", e -> JsonUtils.readVanillaBoundingBox(e.getAsJsonObject()));

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        this.wgRegion = manager.getRegion(regionName);
    }

    public void testCanPurchase(CrownUser user, Balances balances, MarketRegion shops) throws CommandSyntaxException {
        if(balances.canAfford(user.getUniqueId(), getPrice())) throw FtcExceptionProvider.cannotAfford(getPrice());
        if(shops.get(user.getUniqueId()) != null) throw FtcExceptionProvider.translatable("markets.alreadyOwner");
    }

    public void purchase(CrownUser user, Balances balances) {
        balances.remove(user.getUniqueId(), price);

        owner = user.getUniqueId();
        wgRegion.getMembers().addPlayer(user.getUniqueId());
    }

    public void unclaim() {
        for (ShopEntrance e: doors) {
            e.onUnclaim(world);
        }

        BoundingBoxes.copyTo(world, exampleArea, realMin.toWorldVector(world));

        wgRegion.getMembers().removePlayer(owner);
        this.owner = null;
    }

    public ProtectedRegion getWorldGuardRegion() {
        return wgRegion;
    }

    public ShopEntrance[] getDoors() {
        return doors;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public World getWorld() {
        return world;
    }

    public BoundingBox getExampleArea() {
        return exampleArea;
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        json.add("price", price);
        json.add("exampleArea", JsonUtils.writeVanillaBoundingBox(exampleArea));
        json.add("realMin", realMin.serialize());
        json.add("world", world.getName());
        json.addArray("doors", doors);

        if(owner != null) json.addUUID("owner", owner);

        return json.getSource();
    }
}
