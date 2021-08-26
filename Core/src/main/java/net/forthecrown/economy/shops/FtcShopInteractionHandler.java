package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Balances;
import net.forthecrown.events.ShopInteractionListener;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
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
    public void handleInteraction(SignShop shop, Player player, Balances balances) {
        //checks if they're the owner and if they're sneaking, then opens the shop inventory to edit it
        if(player.isSneaking() && (shop.getOwner().equals(player.getUniqueId()) || player.hasPermission(Permissions.FTC_ADMIN))){
            player.openInventory(shop.getInventory());
            Bukkit.getPluginManager().registerEvents(new ShopInteractionListener.ShopRestockListener(player, shop), Crown.inst());
            return;
        }

        CrownUser user = UserManager.getUser(player);
        SignShopSession session = getOrCreateSession(user, shop);
        ShopInteraction interaction = session.getType().getInteraction();

        //If not already on cooldown, add to cooldown
        if(!isOnExpiryCooldown(session)) doSessionExpiryCooldown(session);

        //Test flags
        if(!interaction.testFlags(session)) return;

        //Test the test method and catch any potential exceptions
        try {
            interaction.test(session, balances);

            //Only reset cooldown if test is successful
            doSessionExpiryCooldown(session);
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(player, e);
            //Exception thrown, check failed
            return;
        }

        if(shop.hasTemplate()) shop.getTemplate().onShopUse(session);

        interaction.interact(session, balances);
    }

    @Override
    public SignShopSession getSession(CrownUser user) {
        return sessions.get(user.getUniqueId());
    }

    @Override
    public SignShopSession getOrCreateSession(CrownUser user, SignShop shop) {
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
        UUID key = session.getUser().getUniqueId();

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
        UUID id = session.getUser().getUniqueId();

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

        //Log interaction data if needed
        if(session.getType().isAdmin()) {
            if(ComVars.logAdminShopUsage()) Crown.logger().info(logInfo(session));
        } else if(ComVars.logNormalShopUsage()) Crown.logger().info(logInfo(session));
    }

    @Override
    public boolean isOnExpiryCooldown(SignShopSession session) {
        return deletionDelay.containsKey(session.getUser().getUniqueId());
    }

    //Create the log info string for the session
    private String logInfo(SignShopSession session) {
        return session.getUser().getName() + " " +
                (session.getType().isBuyType() ? "bought" : "sold") + " " +
                session.getAmount() + " " +
                FtcFormatter.normalEnum(session.getMaterial()) +
                " at a" + (session.getType().isAdmin() ? "n admin" : "") + " shop, location: " + session.getShop().getName();
    }
}
