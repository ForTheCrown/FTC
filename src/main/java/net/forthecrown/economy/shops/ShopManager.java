package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.User;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager implements SerializableObject {

    private final Map<WorldVec3i, SignShop> loadedShops = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, SignShopSession> sessions = new HashMap<>();

    /**
     * Handles a single customer right-clicking a shop
     * <p>
     * This method will also check if the player should be shown
     * the shop's inventory instead of interacting with the shop as
     * one normally would.
     * <p>
     * The interaction itself is logged most of the time and is conducted
     * through {@link SignShopSession} instances. The session itself
     * doesn't run any shop logic or ensure the user can actually use
     * the shop, that job is left to the shop's {@link ShopInteraction},
     * which is determined by the shop's type.
     *
     * @param customer The customer using the shop
     * @param shop The shop being interacted with
     * @see ShopInteraction
     * @see SignShopSession
     */
    public void handleInteraction(User customer, SignShop shop) {
        var player = customer.getPlayer();

        if (player.isSneaking() &&
                (SignShops.mayEdit(shop, player.getUniqueId())
                        || player.hasPermission(Permissions.SHOP_ADMIN)
                )
        ) {
            shop.delayUnload();
            player.openInventory(shop.getInventory());

            return;
        }

        var session = getSession(customer.getUniqueId());

        // If this customer isn't in a session, create one
        if (session == null) {
            session = createSession(customer, shop);
        } else {
            // If they are in a session and that session's
            // shop is different from this one, expire that
            // session and start a new one
            if (!session.getShop().equals(shop)) {
                session.expire();

                session = createSession(customer, shop);
            }
        }

        // Test the interaction type, if no errors
        // are thrown, we passed the test and can
        // move on to interaction
        try {
            shop.getType().getInteraction().test(session);

            session.delayExpiry();
            shop.delayUnload();

            // Run the actual interaction logic and
            // tell the user they used the shop
            session.run();
        } catch (CommandSyntaxException e) {
            Exceptions.handleSyntaxException(customer, e);

            Tasks.cancel(session.expireTask);
            session.expire();
        }
    }

    /**
     * Creates and a session for the given customer and shop.
     * The created session is also added to the session map
     *
     * @param customer The customer to create a session for
     * @param shop The shop the customer is interacting with
     * @return The created session
     */
    private SignShopSession createSession(User customer, SignShop shop) {
        // Create and add session
        var session = new SignShopSession(shop, customer);
        addSession(session);

        // Start the expiry count down
        session.delayExpiry();

        return session;
    }

    public SignShopSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public void addSession(SignShopSession session) {
        sessions.put(session.getCustomer().getUniqueId(), session);
    }

    public void removeSession(SignShopSession session) {
        sessions.remove(session.getCustomer().getUniqueId());
    }

    /**
     * Gets a shop at the given location
     * @param vec The location of the sign
     * @return The shop at the location, null if no shop exists at the given location
     */
    public SignShop getShop(WorldVec3i vec) {
        // If the shop is already loaded into
        // memory, then return it
        SignShop result = loadedShops.get(vec);

        if (result != null) {
            return result;
        }

        // If the block is not a shop, return null
        if (!SignShops.isShop(vec.getBlock())) {
            return null;
        }

        result = new SignShop(vec);
        loadedShops.put(vec, result);
        result.delayUnload();

        return result;
    }

    /**
     * Creates a sign shop at the given location
     * @param vec The shop's location
     * @param type The shop's type
     * @param price The shop's starting price
     * @param owner The UUID of the owner
     * @return The created shop
     */
    public SignShop createSignShop(WorldVec3i vec, ShopType type, int price, UUID owner) {
        // Create the shop and add it
        SignShop shop = new SignShop(vec, type, price, owner);
        loadedShops.put(vec, shop);

        // Then return it
        return shop;
    }

    /**
         * Saves all shops and the shop list
         */
    public void save() {
        loadedShops.values().forEach(SignShop::update);
    }

    /**
         * Reloads all shops and the shop list
         */
    public void reload() {
        loadedShops.values().forEach(SignShop::load);
    }

    /**
     * Removes a shop from the loaded
     * shops list
     * @param shop The shop to remove
     */
    public void removeShop(SignShop shop) {
        loadedShops.remove(shop.getPosition());
    }

    /**
     * Gets a shop at the given location
     * @param block The sign's block
     * @return The shop at the location, null if no shop exists at the given location
     */
    public SignShop getShop(Block block) {
        return getShop(new WorldVec3i(block.getWorld(), block.getX(), block.getY(), block.getZ()));
    }

    /**
     * Gets a shop from a given name
     * @param name The shop's name
     * @return The shop with the given name
     */
    public SignShop getShop(LocationFileName name) {
        return getShop(name.toVector());
    }

    /**
     * Creates a sign shop at the given location
     * @param location The shop's location
     * @param shopType The shop's type
     * @param price The shop's starting price
     * @param ownerUUID The UUID of the owner
     * @return The created shop
     */
    public SignShop createSignShop(Location location, ShopType shopType, int price, UUID ownerUUID) {
        return createSignShop(WorldVec3i.of(location), shopType, price, ownerUUID);
    }
}