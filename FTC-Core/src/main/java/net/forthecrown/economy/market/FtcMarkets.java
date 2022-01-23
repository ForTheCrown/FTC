package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMarketData;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.transformation.RegionCopyPaste;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.Validate;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static net.forthecrown.utils.transformation.BoundingBoxes.createPaste;
import static net.forthecrown.utils.transformation.BoundingBoxes.wgToNms;

public class FtcMarkets extends AbstractJsonSerializer implements Markets {
    //2 maps for tracking shops, byName stores all saved shops
    private final Object2ObjectMap<UUID, MarketShop> byOwner = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, MarketShop> byName = new Object2ObjectOpenHashMap<>();

    public FtcMarkets() {
        super("market_region");

        reload();
    }

    @Override
    public MarketShop get(UUID owner) {
        return byOwner.get(owner);
    }

    @Override
    public MarketShop get(String claimName) {
        return byName.get(claimName);
    }

    @Override
    public World getWorld() {
        //Return the OVERWORLD constant in Worlds
        return Worlds.OVERWORLD;
    }

    @Override
    public void add(MarketShop claim) {
        if(claim.hasOwner()) byOwner.put(claim.getOwner(), claim);
        byName.put(claim.getName(), claim);
    }

    @Override
    public void attemptPurchase(MarketShop claim, CrownUser user) throws CommandSyntaxException {
        UserMarketData ownership = user.getMarketData();

        //If they already own a shop
        if(ownership.currentlyOwnsShop()) {
            throw FtcExceptionProvider.translatable("market.alreadyOwner");
        }

        //If the shop already has an owner, idk how this could even be triggered lol
        if(claim.hasOwner()) throw FtcExceptionProvider.translatable("market.alreadyOwned");

        //Check if they can even buy it
        Markets.checkCanPurchase(ownership);

        //Check if they can afford it
        Economy economy = Crown.getEconomy();
        if(!economy.has(user.getUniqueId(), claim.getPrice())) {
            throw FtcExceptionProvider.cannotAfford(claim.getPrice());
        }

        economy.remove(user.getUniqueId(), claim.getPrice());

        //Claim it
        user.sendMessage(Component.translatable("market.bought", NamedTextColor.YELLOW));
        claim(claim, user);
    }

    @Override
    public void claim(MarketShop claim, CrownUser user) {
        Validate.isTrue(!claim.hasOwner(), "Market already has owner");

        UserMarketData ownership = user.getMarketData();
        if(!ownership.hasOwnedBefore()) ownership.setOwnershipBegan(System.currentTimeMillis());
        ownership.setOwnedName(claim.getName());
        ownership.setLastStatusChange();

        claim.setOwner(user.getUniqueId());
        claim.setDateOfPurchase(new Date());

        claim.getWorldGuard().getMembers().addPlayer(user.getUniqueId());

        byOwner.put(user.getUniqueId(), claim);

        for (ShopEntrance e: claim.getEntrances()) {
            e.onClaim(user, getWorld());
        }

        Crown.getGuild().checkVoteShouldContinue();

        //make backup underground to reset store later
        createPaste(
                getWorld(),
                wgToNms(claim.getWorldGuard()),
                claim.getBackupPos().toWorldVector(getWorld())
        )
                .addFilter(new MarketFilters.IgnoreCopyEntrance())
                .addFilter(new MarketFilters.IgnoreShop())
                .runSync();
    }

    @Override
    public void unclaim(MarketShop shop, boolean complete) {
        Validate.isTrue(shop.hasOwner(), "Market has no owner");

        if(shop.isMerged()) unmerge(shop);

        CrownUser owner = shop.ownerUser();
        UserMarketData ownership = owner.getMarketData();
        ownership.setOwnedName(null);
        ownership.setLastStatusChange();

        shop.setDateOfPurchase(null);
        shop.setMerged(null);
        shop.setOwner(null);
        shop.setEvictionDate(null);

        shop.getWorldGuard().getMembers().clear();
        shop.getCoOwners().clear();

        Crown.getGuild().checkVoteShouldContinue();

        if(complete) {
            ownership.setOwnershipBegan(0L);
            resetFromBackup(shop);
        }

        for (ShopEntrance e: shop.getEntrances()) {
            e.onUnclaim(getWorld(), shop);
        }
    }

    @Override
    public void merge(MarketShop shop, MarketShop merged) {
        Validate.isTrue(!shop.equals(merged), "Same shops given in parameters");

        shop.setMerged(merged);
        merged.setMerged(shop);

        merged.getWorldGuard().getMembers().addPlayer(shop.getOwner());
        shop.getWorldGuard().getMembers().addPlayer(merged.getOwner());

        for (UUID id: shop.getCoOwners()) {
            merged.getWorldGuard().getMembers().addPlayer(id);
        }

        for (UUID id: merged.getCoOwners()) {
            shop.getWorldGuard().getMembers().addPlayer(id);
        }

        Crown.getGuild().checkVoteShouldContinue();
    }

    @Override
    public void unmerge(MarketShop shop) {
        Validate.isTrue(shop.isMerged(), "Given shop was not merged");

        MarketShop merged = shop.getMerged();

        merged.setMerged(null);
        shop.setMerged(null);

        shop.getWorldGuard().getMembers().removePlayer(merged.getOwner());
        merged.getWorldGuard().getMembers().removePlayer(shop.getOwner());

        for (UUID id: merged.getCoOwners()) {
            shop.getWorldGuard().getMembers().removePlayer(id);
        }

        for (UUID id: shop.getCoOwners()) {
            merged.getWorldGuard().getMembers().removePlayer(id);
        }

        Crown.getGuild().checkVoteShouldContinue();
    }

    @Override
    public void trust(MarketShop shop, UUID uuid) {
        shop.getCoOwners().add(uuid);
        shop.getWorldGuard().getMembers().addPlayer(uuid);

        if(shop.isMerged()) {
            MarketShop merged = shop.getMerged();
            merged.getWorldGuard().getMembers().addPlayer(uuid);
        }
    }

    @Override
    public void untrust(MarketShop shop, UUID uuid) {
        shop.getCoOwners().remove(uuid);
        shop.getWorldGuard().getMembers().removePlayer(uuid);

        if(shop.isMerged()) {
            MarketShop merged = shop.getMerged();
            merged.getWorldGuard().getMembers().removePlayer(uuid);
        }
    }

    @Override
    public void removeEntrance(MarketShop shop, int index) {
        ShopEntrance entrance = shop.getEntrances().get(index);

        entrance.removeSign(getWorld());
        entrance.removeNotice(getWorld());

        shop.getEntrances().remove(index);
    }

    @Override
    public void addEntrance(MarketShop shop, ShopEntrance entrance) {
        shop.getEntrances().add(entrance);
    }

    @Override
    public boolean areConnected(MarketShop shop, MarketShop other) {
        return shop.getConnectedNames().contains(other.getName());
    }

    @Override
    public void connect(MarketShop shop, MarketShop other) {
        shop.getConnectedNames().add(other.getName());
        other.getConnectedNames().add(shop.getName());
    }

    @Override
    public void disconnect(MarketShop shop, MarketShop other) {
        shop.getConnectedNames().remove(other.getName());
        other.getConnectedNames().remove(shop.getName());
    }

    @Override
    public void remove(MarketShop shop) {
        if(shop.hasOwner()) byOwner.remove(shop.getOwner());

        byName.remove(shop.getName());

        for (ShopEntrance e: shop.getEntrances()) {
            e.removeNotice(getWorld());
            e.removeSign(getWorld());
        }

        ProtectedRegion region = shop.getWorldGuard();

        if(shop.isMerged()) unmerge(shop);

        if(shop.hasOwner()) {
            CrownUser user = shop.ownerUser();
            shop.setOwner(null);

            region.getMembers().removePlayer(user.getUniqueId());

            UserMarketData ownership = user.getMarketData();
            ownership.setOwnedName(null);
            ownership.setOwnershipBegan(0L);

            if(!shop.getCoOwners().isEmpty()) {
                for (UUID id: shop.getCoOwners()) {
                    region.getMembers().removePlayer(id);
                }
            }
        }
    }

    @Override
    public void transfer(MarketShop shop, UUID target) {
        Validate.isTrue(shop.hasOwner(), "Shop has no owner");

        CrownUser user = UserManager.getUser(target);

        shop.setOwner(target);
        shop.getCoOwners().clear();

        ProtectedRegion region = shop.getWorldGuard();
        region.getMembers().clear();
        region.getMembers().addPlayer(target);

        for (ShopEntrance e: shop.getEntrances()) {
            e.onClaim(user, getWorld());
        }

        user.unloadIfOffline();
        Crown.getGuild().checkVoteShouldContinue();
    }

    @Override
    public void resetFromBackup(MarketShop shop) {
        //Figure out positions for pasting
        WorldVec3i pastePos = shop.getMin().toWorldVector(getWorld());
        Vector3i size = shop.getSize();
        Vector3i backup = shop.getBackupPos();

        BoundingBox box = new BoundingBox(
                backup.getX(), backup.getY(), backup.getZ(),
                size.getX() + backup.getX(),
                size.getY() + backup.getY(),
                size.getZ() + backup.getZ()
        );

        RegionCopyPaste paste = createPaste(getWorld(), box, pastePos)
                // ignore any notice heads or door signs
                .addFilter(new MarketFilters.IgnoreNotice())
                .addFilter(new MarketFilters.IgnorePasteEntrance())

                // destroy shops before pasting
                .addPreProcessor(new ShopDestroyPreprocessor());

        paste.runSync();

        paste
                .getOrigin()
                .getLivingEntities()
                .stream()
                .filter(e -> e.getType() != EntityType.PLAYER)
                .forEach(entity -> entity.setHealth(0D));
    }

    @Override
    public void clear() {
        for (MarketShop m: getAllShops()) {
            m.setEvictionDate(null);
        }

        byName.clear();
        byOwner.clear();
    }

    @Override
    public int size() {
        return byName.size();
    }

    @Override
    public Set<UUID> getOwners() {
        return byOwner.keySet();
    }

    @Override
    public Set<String> getNames() {
        return byName.keySet();
    }

    @Override
    public Collection<MarketShop> getAllShops() {
        return byName.values();
    }

    @Override
    public Collection<MarketShop> getOwnedShops() {
        return byOwner.values();
    }

    @Override
    public Book getPurchaseBook(MarketShop shop, CrownUser user, ClickableTextNode node) {
        Book.Builder builder = Book.builder()
                .title(Component.text("Purchase shop?"));

        Component newLine = Component.newline();
        Component tripleNew = Component.text("\n\n\n");

        TextComponent.Builder textBuilder = Component.text()
                .content("Purchase shop?")

                .append(tripleNew)
                .append(Component.text("Price: ").append(FtcFormatter.rhinesNonTrans(shop.getPrice())))
                .append(tripleNew);

        int entranceAmount = shop.getEntrances().size();

        textBuilder.append(
                Component.text(entranceAmount + " entrance" + FtcUtils.addAnS(entranceAmount))
        );

        Vector3i size = shop.getSize();
        String dimensions = size.getX() + "x" + size.getY() + "x" + size.getY() + " blocks";

        textBuilder
                .append(newLine)
                .append(Component.text(dimensions));

        textBuilder
                .append(tripleNew)
                .append(newLine)
                .append(newLine);

        textBuilder.append(node.prompt(user));

        builder.addPage(textBuilder.build());
        return builder.build();
    }

    @Override
    public void refresh(MarketShop shop) {
        for (ShopEntrance e: shop.getEntrances()) {
            if(shop.hasOwner()) e.onClaim(shop.ownerUser(), getWorld());
            else e.onUnclaim(getWorld(), shop);
        }
    }

    @Override
    protected void save(JsonWrapper json) {
        json.addList("entries", byName.values());
    }

    @Override
    protected void reload(JsonWrapper json) {
        clear();

        if(json.has("entries")) {
            for (JsonElement e: json.getArray("entries")) {
                add(FtcMarketShop.fromJson(e));
            }
        }
    }
}
