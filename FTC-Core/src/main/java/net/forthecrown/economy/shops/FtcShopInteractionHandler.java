package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.events.ShopInteractionListener;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class FtcShopInteractionHandler implements ShopInteractionHandler {
    private final Map<UUID, SignShopSession> sessions = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, BukkitRunnable> deletionDelay = new Object2ObjectOpenHashMap<>();

    @Override
    public void handleInteraction(SignShop shop, ShopCustomer customer, Economy economy) {
        //checks if they're the owner and if they're sneaking, then opens the shop inventory to edit it
        if(customer instanceof CrownUser) {
            Player user = ((CrownUser) customer).getPlayer();

            if(user.isSneaking() && (shop.getOwnership().mayEditShop(user.getUniqueId()) || user.hasPermission(Permissions.SHOP_ADMIN))) {
                user.openInventory(shop.getInventory());
                Bukkit.getPluginManager().registerEvents(new ShopInteractionListener.ShopRestockListener(user, shop), Crown.inst());
                return;
            }
        }

        SignShopSession session = getOrCreateSession(customer, shop);
        ShopInteraction interaction = shop.getType().getInteraction();

        //If not already on cooldown, add to cooldown
        if(!isOnExpiryCooldown(session)) doSessionExpiryCooldown(session);

        //Test the test method and catch any potential exceptions
        try {
            //Test type-specific requirements
            interaction.test(session, economy);

            //Only reset cooldown if test is successful
            doSessionExpiryCooldown(session);
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(customer, e);
            //Exception thrown, check failed
            return;
        }

        interaction.interact(session, economy);
    }

    @Override
    public SignShopSession getSession(ShopCustomer user) {
        return sessions.get(user.getUniqueId());
    }

    @Override
    public SignShopSession getOrCreateSession(ShopCustomer user, SignShop shop) {
        if(sessions.containsKey(user.getUniqueId())) {
            SignShopSession session = sessions.get(user.getUniqueId());
            if(shop.equals(session.getShop())) return session;

            removeAndLog(session);
        }

        SignShopSession session = new SignShopSession(shop, user);
        sessions.put(user.getUniqueId(), session);

        return session;
    }

    @Override
    public void doSessionExpiryCooldown(SignShopSession session) {
        UUID key = session.getCustomer().getUniqueId();

        if(deletionDelay.containsKey(key)) {
            try {
                deletionDelay.get(key).cancel();
            } catch (Exception ignored) {}
            deletionDelay.remove(key);
        }

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                removeAndLog(session);
            }
        };
        runnable.runTaskLaterAsynchronously(Crown.inst(), 2*20);

        deletionDelay.put(key, runnable);
    }


    @Override
    public void removeAndLog(SignShopSession session) {
        UUID id = session.getCustomer().getUniqueId();

        //Remove and stop all delays
        sessions.remove(id);
        if(deletionDelay.containsKey(id)) {
            BukkitRunnable runnable = deletionDelay.get(id);

            try {
                runnable.cancel();
            } catch (Exception ignored) {}
            deletionDelay.remove(id);
        }

        //If the session has expiry code to execute, run it
        if(session.getOnSessionExpire() != null) session.getOnSessionExpire().run();

        // Record session in history
        // Amount will be 0 for sessions that didn't pass the
        // interaction test
        if(session.getAmount() > 0) {
            session.getShop().getHistory().addEntry(session);
        }

        //Log interaction data if needed
        if(session.getType().isAdmin()) {
            if(FtcVars.logAdminShop.get()) Crown.logger().info(logInfo(session));
        } else if(FtcVars.logNormalShop.get()) Crown.logger().info(logInfo(session));
    }

    @Override
    public boolean isOnExpiryCooldown(SignShopSession session) {
        return deletionDelay.containsKey(session.getCustomer().getUniqueId());
    }

    //Create the log info string for the session
    private String logInfo(SignShopSession session) {
        return session.getCustomer().getName() + " " +
                (session.getType().isBuyType() ? "bought" : "sold") + " " +
                session.getAmount() + " " +
                FtcFormatter.normalEnum(session.getMaterial()) +
                " at a" + (session.getType().isAdmin() ? "n admin" : "") + " shop, location: " + session.getShop().getName();
    }
}
