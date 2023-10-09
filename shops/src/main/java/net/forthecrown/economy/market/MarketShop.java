package net.forthecrown.economy.market;

import static net.forthecrown.economy.market.MarketEviction.SOURCE_AUTOMATIC;
import static net.forthecrown.economy.market.MarketReset.TEMPLATE_DEPTH;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.mail.Mail;
import net.forthecrown.text.Text;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.World;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3i;

/**
 * A market shop represents a single player's shop in the spawn region.
 * <p>
 * This system mostly functions by tying these markets to WorldGuard regions and
 * syncing the market's data to the region.
 */
@RequiredArgsConstructor
public class MarketShop {
  /* ----------------------------- CONSTANTS ------------------------------ */

  public static final int UNSET_PRICE = -1;

  /**
   * The maximum number of scans a shop can have before it starts testing to see
   * if the members should be evicted.
   */
  public static final int MAX_SCANS = 3;

  // JSON Keys
  public static final String KEY_PRICE = "price";
  public static final String KEY_ENTRANCES = "entrances";
  public static final String KEY_CONNECTED = "connected";
  public static final String KEY_OWNER = "owner";
  public static final String KEY_PURCHASE_DATE = "purchaseDate";
  public static final String KEY_RESET = "resetData";
  public static final String KEY_SCANS = "scans";
  public static final String KEY_EVICTION = "eviction";
  public static final String KEY_MERGED = "merged";
  public static final String KEY_MEMBERS = "members";
  public static final String KEY_EDITING = "memberEditingAllowed";
  public static final String KEY_CURRENT_OWNER = "currentOwner";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * World guard region of this shop
   */
  @Getter
  private final ProtectedRegion worldGuard;

  /**
   * Owner of this market shop
   */
  @Getter
  @Setter
  private UUID owner;

  /**
   * The time stamp of when this shop was purchased, or -1, if the shop currently has no owner
   */
  @Getter
  @Setter
  private long purchaseDate;

  /**
   * the UUIDs of every player that is a co owner in this shop
   */
  @Getter
  private final Set<UUID> members = new ObjectOpenHashSet<>();

  /**
   * Every entrance to the shop
   */
  @Getter
  private final List<ShopEntrance> entrances = new ObjectArrayList<>();

  /**
   * The names of every shop connected to this one
   */
  @Getter
  private final Set<String> connected = new ObjectOpenHashSet<>();

  /**
   * True, if members are allowed to edit each-others' shops, false otherwise
   */
  @Getter
  @Setter
  private boolean memberEditingAllowed = true;

  /**
   * Market's current eviction data, will be null if no eviction in progress
   */
  @Getter
  private MarketEviction eviction;

  private int price = UNSET_PRICE;

  /**
   * The name of the shop this shop is merged with
   */
  private String mergedName;

  @Getter
  @Setter
  private MarketReset reset;

  private final List<MarketScan> scans = new ObjectArrayList<>();

  MarketManager manager;

  private ClickEvent purchaseClickEvent;

  /* ----------------------------- ACCESSORS ------------------------------ */

  /**
   * Gets the name of the worldguard region this shop is linked to
   *
   * @return The underlying worldguard region
   */
  public String getName() {
    return worldGuard.getId();
  }

  /**
   * Gets the display name of the worldguard region
   *
   * @return The world guard region's display name
   */
  public Component wgDisplayName() {
    return text('[' + getName() + ']')
        .color(NamedTextColor.AQUA)
        .hoverEvent(text("Click for info"))
        .clickEvent(
            ClickEvent.runCommand("/rg -w \"world\" info " + getName())
        );
  }

  /**
   * Tests if this shop has been marked for eviction
   *
   * @return True, if {@link #getEviction()} is not null, false otherwise
   */
  public boolean markedForEviction() {
    return eviction != null;
  }

  public boolean hasOwner() {
    return getOwner() != null;
  }

  /**
   * Checks whether the shop is merged
   *
   * @return gerMerged() != null
   */
  public boolean isMerged() {
    return getMerged() != null;
  }

  /**
   * Gets the user object of the owner
   *
   * @return The owner's user object, or null, if not owned
   */
  public User ownerUser() {
    return hasOwner() ? Users.get(getOwner()) : null;
  }

  /**
   * Gets this shop's price, if this market's {@link #price} field is equal to
   * {@link #UNSET_PRICE} then this method will return the default price instead. The default price
   * is set in the plugin's config
   *
   * @return The shop's effective price
   */
  public int getPrice() {
    var config = manager.getPlugin().getShopConfig();
    return price == UNSET_PRICE ? config.getDefaultPrice() : price;
  }

  /**
   * Sets the shop's price
   */
  public void setPrice(int price) {
    this.price = price;
  }

  /**
   * Sets the shop's eviction data
   * <p>
   * If there is an already ongoing eviction, it will be cancelled and the given
   * eviction will be set and started in its stead.
   *
   * @param eviction The eviction data to set
   */
  public void setEviction(MarketEviction eviction) {
    if (this.eviction != null) {
      this.eviction.cancel();
    }

    this.eviction = eviction;

    if (eviction != null) {
      eviction.start();
    }
  }

  /* ----------------------------- CLAIMING AND UNCLAIMING ------------------------------ */


  public Component purchaseButton(User user) {
    boolean canAfford = user.hasBalance(getPrice());

    TextColor color = canAfford
        ? NamedTextColor.GREEN
        : NamedTextColor.GRAY;

    Component text = text("[Purchase]", color);

    if (!canAfford) {
      text = text.hoverEvent(text("Cannot afford"));
    }

    if (!Markets.canChangeStatus(user)) {
      text = text.color(NamedTextColor.GOLD).hoverEvent(text("Cannot currently purchase shop"));
    }

    return text.clickEvent(getPurchaseClickEvent());
  }

  private ClickEvent getPurchaseClickEvent() {
    if (purchaseClickEvent != null) {
      return purchaseClickEvent;
    }

    purchaseClickEvent = ClickEvent.callback(
        audience -> {
          User user = Audiences.getUser(audience);

          if (user == null) {
            return;
          }

          try {
            attemptPurchase(user);
          } catch (CommandSyntaxException exc) {
            Exceptions.handleSyntaxException(user, exc);
          }
        },

        builder -> {
          builder.uses(-1).lifetime(Duration.ofDays(1));
        }
    );

    return purchaseClickEvent;
  }

  /**
   * Resets the shop using the data in {@link #getReset()}
   *
   * @return True, if shop was successfully reset, false if {@link #reset} ==
   * null
   */
  public boolean reset() {
    //Figure out positions for pasting
    if (reset == null) {
      return false;
    }

    reset.place(Markets.getWorld());
    return true;
  }

  /**
   * Attempts to purchase this shop
   *
   * @param user The user purchasing
   * @throws CommandSyntaxException If the user cannot purchase this shop
   */
  public void attemptPurchase(User user) throws CommandSyntaxException {
    //If they already own a shop
    if (Markets.ownsShop(user)) {
      if (owner.equals(user.getUniqueId())) {
        throw Exceptions.create("You already own THIS shop lol");
      }

      throw EconExceptions.MARKET_ALREADY_OWNER;
    }

    // Alt accounts are not allowed to purchase market shops
    var service = Users.getService();
    if (service.isAltAccount(user.getUniqueId())) {
      throw EconExceptions.ALTS_CANNOT_OWN;
    }

    // If the shop already has an owner, could be triggered in the
    // unlikely scenario where 2 people open the purchase books, 1
    // buys it and then the other person attempts to purchase as well
    if (hasOwner()) {
      throw EconExceptions.MARKET_ALREADY_OWNED;
    }

    //Check if they can even buy it
    Markets.checkCanPurchase(user);
    int price = getPrice();

    //Check if they can afford it
    if (!user.hasBalance(price)) {
      throw Exceptions.cannotAfford(price);
    }

    user.removeBalance(price);

    //Claim it
    user.sendMessage(EconMessages.marketBought(price));
    claim(user);
  }

  /**
   * Claims this shop for the given user
   * <p>
   * This method will update user's {@link TimeField#MARKET_LAST_ACTION} and
   * {@link TimeField#MARKET_OWNERSHIP_STARTED} fields. This will also make a
   * copy of the shop 40 blocks under the market itself and then set that to be
   * the shop's {@link #reset}
   * <p>
   * This also updates the shop's entrances and world guard region
   *
   * @param user The user claiming the shop
   * @throws IllegalArgumentException If the shop already has an owner
   */
  public void claim(User user) throws IllegalArgumentException {
    Validate.isTrue(!hasOwner(), "Market already has owner");

    var world = Markets.getWorld();

    user.setTimeToNow(TimeField.MARKET_LAST_ACTION);

    if (user.getTime(TimeField.MARKET_OWNERSHIP_STARTED) == -1) {
      user.setTimeToNow(TimeField.MARKET_OWNERSHIP_STARTED);
    }

    setOwner(user.getUniqueId());
    setPurchaseDate(System.currentTimeMillis());

    worldGuard.getMembers().addPlayer(user.getUniqueId());

    manager.onShopClaim(this);

    for (ShopEntrance e : entrances) {
      e.onClaim(user, world);
    }

    Vector3i wgMin = fromWorldEdit(worldGuard.getMinimumPoint());
    Vector3i wgMax = fromWorldEdit(worldGuard.getMaximumPoint());

    MarketReset reset = new MarketReset(
        wgMin,
        wgMin.sub(0, TEMPLATE_DEPTH, 0),
        wgMax.sub(wgMin)
    );

    reset.copy(world);
    setReset(reset);
  }

  private static Vector3i fromWorldEdit(BlockVector3 bvec) {
    return Vector3i.from(bvec.getX(), bvec.getY(), bvec.getZ());
  }

  /**
   * Unclaims the shop and updates the worldguard region, shop entrances and
   * user time field as well as clearing the scans list and co-owners list.
   * <p>
   * If <code>complete == true</code>, then the shop is also reset and the
   * owner's {@link TimeField#MARKET_OWNERSHIP_STARTED} field is removed.
   *
   * @param complete True to remove the user's
   *                 {@link TimeField#MARKET_OWNERSHIP_STARTED} field and to
   *                 reset the shop.
   * @throws IllegalArgumentException If the shop has no owner
   */
  public void unclaim(boolean complete) throws IllegalArgumentException {
    Validate.isTrue(hasOwner(), "Market has no owner");
    var world = Markets.getWorld();

    if (isMerged()) {
      unmerge();
    }

    User owner = ownerUser();
    owner.setTimeToNow(TimeField.MARKET_LAST_ACTION);

    manager.onShopUnclaim(this);

    setPurchaseDate(-1);
    setOwner(null);
    setEviction(null);

    worldGuard.getMembers().clear();
    members.clear();
    scans.clear();

    if (complete) {
      owner.setTime(TimeField.MARKET_OWNERSHIP_STARTED, -1);
      reset();
    }

    for (ShopEntrance e : entrances) {
      e.onUnclaim(world, this);
    }
  }

  /**
   * Transfers this shop to the given target. This used to be a separate method
   * for a reason, however it nows just calls {@link #unclaim(boolean)} with the
   * boolean parameter as false, and then calls {@link #claim(User)} for the
   * given user.
   *
   * @param target The user to transfer the shop to
   * @throws IllegalArgumentException If the shop has no owner
   */
  public void transfer(User target) throws IllegalArgumentException {
    unclaim(false);
    claim(target);
  }

  /* ----------------------------- MERGING ------------------------------ */

  /**
   * Merges this shop with the given shop and syncs the world guard data of both
   * shops
   *
   * @param other The shop to merge with
   * @throws IllegalArgumentException Thrown either if the shop is already
   *                                  merged, or if the given shop is this shop
   */
  public void merge(MarketShop other) throws IllegalArgumentException {
    Validate.isTrue(!isMerged(), "Shop is already merged");
    Validate.isTrue(!equals(other), "Same shops given in parameters");

    setMerged(other);
    other.setMerged(this);

    other.syncWorldGuard();
    this.syncWorldGuard();
  }

  /**
   * Unmerges the shop and syncs the data of both shops to their world guard
   * regions
   *
   * @throws IllegalArgumentException If the shop is not merged
   */
  public void unmerge() throws IllegalArgumentException {
    Validate.isTrue(isMerged(), "Given shop was not merged");

    MarketShop merged = getMerged();

    merged.setMerged(null);
    setMerged(null);

    syncWorldGuard();
    merged.syncWorldGuard();
  }

  /**
   * Gets the shop this shop is 'merged' with
   *
   * @return This shop's merged shop, or null, if not merged
   */
  public MarketShop getMerged() {
    return Strings.isNullOrEmpty(mergedName) ? null : manager.get(mergedName);
  }

  /**
   * Sets the shop this shop is merged with
   * <p>
   * Be aware, this is just a setter for a single value, if you're looking for a
   * setter to keep data between shops synced, use {@link #merge(MarketShop)}
   * and {@link #unmerge()}
   *
   * @param shop The shop to set, null, to unmerge
   */
  public void setMerged(MarketShop shop) {
    mergedName = shop == null ? null : shop.getName();
  }

  /* ---------------------------- MEMBERSHIPS ----------------------------- */

  /**
   * Adds the given player's UUID to this shop's members list
   *
   * @param uuid The ID to add to the members list
   * @throws IllegalArgumentException If the shop has no owner
   */
  public void trust(UUID uuid) throws IllegalArgumentException {
    Validate.isTrue(hasOwner(), "Market has no owner");

    members.add(uuid);
    syncWorldGuard();
  }

  /**
   * Removes the given player's UUID from this shop's members list
   *
   * @param uuid The ID to remove
   * @throws IllegalArgumentException If the shop has no owner
   */
  public void untrust(UUID uuid) throws IllegalArgumentException {
    Validate.isTrue(hasOwner(), "Market has no owner");

    members.remove(uuid);
    syncWorldGuard();
  }

  /* ---------------------------- CONNECTIONS ----------------------------- */

  /**
   * Connects this shop to the given shop
   *
   * @param other The shop to connect
   */
  public void connect(MarketShop other) {
    connected.add(other.getName());
    other.connected.add(getName());
  }

  /**
   * Disconnects this shop from the given shop
   *
   * @param shop The shop to disconnect from
   */
  public void disconnect(MarketShop shop) {
    connected.remove(shop.getName());
    shop.connected.remove(getName());
  }

  public boolean isConnected(MarketShop other) {
    return other.connected.contains(getName())
        && connected.contains(other.getName());
  }

  /* ----------------------------- ENTRANCES ------------------------------ */

  public void addEntrance(ShopEntrance entrance) {
    entrances.add(entrance);
  }

  public void removeEntrance(int index) {
    var entrance = entrances.remove(index);

    var world = Markets.getWorld();
    entrance.removeSign(world);
    entrance.removeNotice(world);
  }

  /* ----------------------------- EVICTION ------------------------------ */

  public void beginEviction(Duration delay, Component reason, String source) {
    long date = System.currentTimeMillis() + delay.toMillis();
    beginEviction(date, reason, source);
  }

  /**
   * Begins an eviction of this shop.
   * <p>
   * This will alert the user their shop has been marked for eviction with
   * either a message and/or sending them a mail, depending on if they're
   * online.
   * <p>
   * Otherwise, it just creates an {@link MarketEviction} instance and calls
   * {@link #setEviction(MarketEviction)}.
   *
   * @param evictionDate The timestamp of when the eviction will occur
   * @param reason       The reason for the eviction
   * @param source       The text name of the entity issuing the eviction, will
   *                     either be the staff member's playername of
   *                     {@link MarketEviction#SOURCE_AUTOMATIC} if it's an
   *                     automatic eviction.
   * @throws IllegalArgumentException If the shop has no owner or is already
   *                                  marked for eviction
   */
  public void beginEviction(long evictionDate,
                            Component reason,
                            String source
  ) throws IllegalArgumentException {
    Validate.isTrue(hasOwner(), "Cannot evict shop with no owner");
    Validate.isTrue(!markedForEviction(),
        "Shop '%s' is already marked for eviction",
        getName()
    );

    MarketEviction data = new MarketEviction(this, evictionDate, reason, source);
    setEviction(data);

    User user = ownerUser();

    Mail mail = Mail.builder()
        .target(user)
        .message(EconMessages.evictionMail(data))
        .send();

    if (user.isOnline()) {
      user.sendMessage(EconMessages.evictionNotice(data));
      mail.toggleRead();
    }

    Loggers.getLogger().info(
        "{}, owner `{}`, has been marked for eviction, reason: `{}`, source: `{}`",
        getName(), user.getNickOrName(), Text.plain(reason), source
    );
  }

  /**
   * Stops this shop's eviction.
   * <p>
   * Will also inform the user that their shop eviction has been cancelled.
   *
   * @throws IllegalArgumentException If the shop has no owner or is NOT marked
   *                                  for eviction
   */
  public void stopEviction() throws IllegalArgumentException {
    Validate.isTrue(hasOwner(), "Shop has no owner");
    Validate.isTrue(markedForEviction(), "Shop '%s' is not marked for eviction", getName());

    setEviction(null);

    User user = ownerUser();

    var message = EconMessages.EVICTION_CANCELLED;
    user.sendMessage(message);

    Mail.builder()
        .target(user)
        .message(message)
        .sendQuietly();

    Loggers.getLogger().info(
        "{}, owner '{}', eviction cancelled",
        getName(),
        user.getNickOrName()
    );
  }

  /**
   * Refreshes a shop
   * <p>
   * Aka, makes sure all the entrances exist
   */
  public void refresh(World marketWorld) {
    for (ShopEntrance e : entrances) {
      if (hasOwner()) {
        e.onClaim(ownerUser(), marketWorld);
      } else {
        e.onUnclaim(marketWorld, this);
      }
    }
  }

  /**
   * Called by {@link MarketManager#onDayChange()} to test if the market should
   * start the automatic eviction process.
   */
  public void validateOwnership() {
    if (!hasOwner()) {
      return;
    }

    var owner = ownerUser();
    var lastOnline = owner.getTime(TimeField.LAST_LOGIN);
    var config = manager.getPlugin().getShopConfig();

    // If owner has been offline for a long time
    if (lastOnline != -1 && Time.isPast(lastOnline + config.getInactiveKickTime().toMillis())) {
      if (!markedForEviction()) {
        beginEviction(
            config.getEvictionDelay(),
            EconMessages.MARKET_EVICT_INACTIVE,
            SOURCE_AUTOMATIC
        );
      }

      return;
    }
    // Cancel eviction if there is one, and if it was
    // started due to inactivity
    else if (eviction != null
        && eviction.getReason().equals(EconMessages.MARKET_EVICT_INACTIVE)
    ) {
      stopEviction();
    }

    long nextScanTime = scans.isEmpty()
        ? 0L
        : scans.get(0).date() + config.getScanInterval().toMillis();

    if (!Time.isPast(nextScanTime)) {
      return;
    }

    MarketScan scan = MarketScan.create(Markets.getWorld(), this, manager.getPlugin().getShops());
    scans.add(0, scan);

    // There's got to be at least MAX_SCANS weeks worth of
    // scans for us to properly assess the shop's
    // state
    if (scans.size() > MAX_SCANS) {
      scans.subList(MAX_SCANS, scans.size()).clear();
    } else {
      return;
    }

    // Tally up Scans
    int failedAmount = 0;
    int failedStock  = 0;
    int failedDate   = 0;

    Duration maxUseless = config.getUnusedShopKickTime();

    for (var s : scans) {
      int totalShops = s.stockedCount() + s.unstockedCount();
      double requiredStock = totalShops * GenericMath.clamp(config.getMinStock(), 0, 1);

      // If there is enough shops and if enough are in stock
      // skip this scan
      if (totalShops >= config.getMinimumShopAmount() && s.stockedCount() >= requiredStock) {
        if (eviction != null && eviction.getSource().equals(SOURCE_AUTOMATIC)) {
          stopEviction();
        }

        return;
      }

      if (requiredStock > s.stockedCount()) {
        failedStock++;
      }

      if (totalShops < config.getMinimumShopAmount()) {
        failedAmount++;
      }

      if (s.averageUseDate() == -1) {
        continue;
      }

      long since = Time.timeSince(s.averageUseDate());

      if (since > maxUseless.toMillis()) {
        failedDate++;
      }
    }

    Component reason = failedAmount < failedStock
        ? EconMessages.MARKET_EVICT_STOCK
        : EconMessages.tooLittleShops();

    if (!markedForEviction()) {
      beginEviction(config.getEvictionDelay(), reason, SOURCE_AUTOMATIC);
    }
  }

  /**
   * Syncs this shop's members and owner to the world guard, factoring in the
   * shop's merged shop as well.
   */
  public void syncWorldGuard() {
    DefaultDomain domain = new DefaultDomain();
    forEachMember(domain::addPlayer);

    var merged = getMerged();
    if (merged != null) {
      merged.forEachMember(domain::addPlayer);
      merged.getWorldGuard().setMembers(domain);
    }

    getWorldGuard().setMembers(domain);
  }

  /**
   * Iterates through each member of the shop including the shop's owner
   *
   * @param consumer The consumer to apply to members
   */
  public void forEachMember(Consumer<UUID> consumer) {
    if (!hasOwner()) {
      return;
    }

    consumer.accept(owner);
    members.forEach(consumer);
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public void serialize(JsonWrapper json) {
    json.add(KEY_PRICE, price);

    if (hasOwner()) {
      JsonWrapper ownership = JsonWrapper.create();

      ownership.addUUID(KEY_OWNER, owner);

      if (purchaseDate != -1) {
        ownership.addTimeStamp(KEY_PURCHASE_DATE, purchaseDate);
      }

      if (markedForEviction()) {
        ownership.add(KEY_EVICTION, eviction.serialize());
      }

      if (mergedName != null) {
        ownership.add(KEY_MERGED, mergedName);
      }

      if (!members.isEmpty()) {
        ownership.addList(KEY_MEMBERS, members, JsonUtils::writeUUID);
      }

      if (!memberEditingAllowed) {
        ownership.add(KEY_EDITING, false);
      }

      json.add(KEY_CURRENT_OWNER, ownership);
    }

    if (!entrances.isEmpty()) {
      json.addList(KEY_ENTRANCES, entrances, ShopEntrance::serialize);
    }

    if (!connected.isEmpty()) {
      json.addList(KEY_CONNECTED, connected, JsonPrimitive::new);
    }

    if (!scans.isEmpty()) {
      json.addList(KEY_SCANS, scans, MarketScan::serialize);
      scans.sort(MarketScan.COMPARATOR);
    }

    if (reset != null) {
      json.add(KEY_RESET, reset.serialize());
    }
  }

  public void deserialize(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    price = json.getInt(KEY_PRICE);

    if (json.has(KEY_CURRENT_OWNER)) {
      JsonWrapper ownership = json.getWrapped(KEY_CURRENT_OWNER);
      assert ownership != null;

      owner = ownership.getUUID(KEY_OWNER);
      purchaseDate = ownership.getTimeStamp(KEY_PURCHASE_DATE, -1);

      members.addAll(ownership.getList(KEY_MEMBERS, JsonUtils::readUUID));

      if (ownership.has("merged")) {
        mergedName = ownership.getString(KEY_MERGED);
      } else {
        mergedName = null;
      }

      memberEditingAllowed = ownership.getBool(KEY_EDITING, true);

      if (ownership.has(KEY_EVICTION)) {
        setEviction(
            MarketEviction.deserialize(ownership.get(KEY_EVICTION), this)
        );
      } else {
        setEviction(null);
      }
    } else {
      owner = null;
      purchaseDate = -1;
      setEviction(null);
      members.clear();
      mergedName = null;
      memberEditingAllowed = true;
    }

    entrances.addAll(json.getList(KEY_ENTRANCES, ShopEntrance::deserialize));
    connected.addAll(json.getList(KEY_CONNECTED, JsonElement::getAsString));

    if (json.has(KEY_SCANS)) {
      scans.addAll(
          json.getList(KEY_SCANS, MarketScan::deserialize)
      );
    }

    if (json.has(KEY_RESET)) {
      reset = MarketReset.deserialize(json.get(KEY_RESET));
    }
  }

  /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MarketShop shop = (MarketShop) o;

    return new EqualsBuilder()
        .append(worldGuard, shop.worldGuard)
        .append(getOwner(), shop.getOwner())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(worldGuard)
        .append(getOwner())
        .toHashCode();
  }

  @Override
  public String toString() {
    return getName();
  }
}