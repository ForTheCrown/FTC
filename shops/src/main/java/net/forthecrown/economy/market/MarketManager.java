package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.math.WorldVec3i;
import org.slf4j.Logger;

public class MarketManager {

  private static final Logger LOGGER = Loggers.getLogger();

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  //2 maps for tracking shops, byName stores all saved shops
  private final Map<UUID, MarketShop> byOwner = new Object2ObjectOpenHashMap<>();
  private final Map<String, MarketShop> byName = new Object2ObjectOpenHashMap<>();

  @Getter
  private final List<MarketEviction> awaitingExecution = new ArrayList<>();

  @Getter
  private final Path directory;

  @Getter
  private final ShopsPlugin plugin;

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  public MarketManager(ShopsPlugin plugin) {
    this.directory = PathUtil.ensureDirectoryExists(PathUtil.pluginPath(plugin, "markets"));
    this.plugin = plugin;
  }

  /* ----------------------------- METHODS ------------------------------ */

  /**
   * Gets a shop by the shop owner's UUID
   *
   * @param owner The owner
   * @return The shop owned by that UUID, null if the UUID has no saved shop
   */
  public MarketShop get(UUID owner) {
    return byOwner.get(owner);
  }

  /**
   * Gets a shop by the name of it's WorldGuard region
   *
   * @param claimName The shop's name
   * @return The shop by that name
   */
  public MarketShop get(String claimName) {
    return byName.get(claimName);
  }

  public MarketShop get(WorldVec3i pos) {
    if (!pos.getWorld().equals(Markets.getWorld())) {
      return null;
    }

    for (var s : getAllShops()) {
      if (s.getWorldGuard().contains(pos.x(), pos.y(), pos.z())) {
        return s;
      }
    }

    return null;
  }

  /**
   * Add the given shop to this manager's control
   *
   * @param claim The shop to add
   */
  public void add(MarketShop claim) {
    if (claim.hasOwner()) {
      byOwner.put(claim.getOwner(), claim);
    }

    byName.put(claim.getName(), claim);
    claim.manager = this;

    // Sync world guard so its members
    // list is the same as the shop's
    claim.syncWorldGuard();
  }

  void onShopClaim(MarketShop shop) {
    byOwner.put(shop.getOwner(), shop);
  }

  void onShopUnclaim(MarketShop shop) {
    byOwner.remove(shop.getOwner());
  }

  /**
   * Removes the given shop from from this market manager
   *
   * @param shop The shop to remove
   */
  public void remove(MarketShop shop) {
    _remove(shop);

    if (shop.getReset() != null) {
      shop.reset();
    }

    for (ShopEntrance e : shop.getEntrances()) {
      e.removeNotice(Markets.getWorld());
      e.removeSign(Markets.getWorld());
    }

    ProtectedRegion region = shop.getWorldGuard();

    if (shop.isMerged()) {
      shop.unmerge();
    }

    for (var m : shop.getConnected()) {
      var connected = get(m);

      if (connected == null) {
        continue;
      }

      shop.disconnect(connected);
    }

    if (shop.hasOwner()) {
      User user = shop.ownerUser();
      shop.setOwner(null);

      user.setTime(TimeField.MARKET_OWNERSHIP_STARTED, -1);
      region.getMembers().clear();
    }
  }

  private void _remove(MarketShop shop) {
    if (shop.hasOwner()) {
      byOwner.remove(shop.getOwner());
    }
    byName.remove(shop.getName());
    shop.manager = null;
  }

  /**
   * Clears all the shops from this manager
   */
  public void clear() {
    for (MarketShop m : getAllShops()) {
      m.setEviction(null);
    }

    byName.clear();
    byOwner.clear();
  }

  /**
   * Gets the amount of shops this manager has
   *
   * @return The shops held by this manager
   */
  public int size() {
    return byName.size();
  }

  /**
   * Gets the names of all the shops
   *
   * @return The shops' names
   */
  public Set<String> getNames() {
    return byName.keySet();
  }

  /**
   * Gets all the shops held by this manager
   *
   * @return All this manager's shops
   */
  public Collection<MarketShop> getAllShops() {
    return byName.values();
  }

  public Collection<MarketShop> getOwnedShops() {
    return byOwner.values();
  }

  public boolean isEmpty() {
    return getAllShops().isEmpty();
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public void load() {
    clear();

    RegionManager manager = WorldGuard.getInstance()
        .getPlatform()
        .getRegionContainer()
        .get(BukkitAdapter.adapt(Markets.getWorld()));

    if (manager == null) {
      LOGGER.warn(
          "World {} has no WG region manager! Cannot load markets",
          Markets.getWorld().getName()
      );

      return;
    }

    Set<MarketShop> notLoaded = new ObjectOpenHashSet<>(byName.values());

    try (var stream = Files.newDirectoryStream(directory)) {
      for (var p : stream) {
        String name = p.getFileName()
            .toString()
            .replaceAll(".json", "");

        if (!manager.hasRegion(name)) {
          LOGGER.warn("Found shop with no matching worldguard region: file='{}', name='{}'", p,
              name);
          continue;
        }

        try {
          JsonElement element = JsonUtils.readFile(p);
          MarketShop shop = get(name);

          if (shop == null) {
            // If the shop doesn't exist, create a shop
            // instance for it
            ProtectedRegion region = manager.getRegion(name);
            shop = new MarketShop(region);
          } else {
            // Temporarily remove shop
            // because owner or members might
            // change and the add(shop); call
            // will sync all that data again
            _remove(shop);
          }

          shop.deserialize(element);
          notLoaded.remove(shop);
          add(shop);

          LOGGER.debug("Loaded market {}", shop.getName());
        } catch (IOException e) {
          LOGGER.error("Error reading market file: '{}'", p, e);
        }
      }

      if (!notLoaded.isEmpty()) {
        for (var shop : notLoaded) {
          remove(shop);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error loading market shops:", e);
    }
  }

  public void save() {
    for (var market : byName.values()) {
      Path marketFile = directory.resolve(market.getName() + ".json");
      SerializationHelper.writeJsonFile(marketFile, market::serialize);
    }
  }
}