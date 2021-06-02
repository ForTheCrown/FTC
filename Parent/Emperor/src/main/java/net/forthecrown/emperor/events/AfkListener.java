package net.forthecrown.emperor.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.emperor.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkListener implements Listener {

    private final CrownUser user;
    private final Player player;

    public AfkListener(CrownUser user){
        this.user = user;
        this.player = user.getPlayer();
    }

    public void checkUnafk(PlayerEvent event){
        checkUnafk(event.getPlayer());
    }

    public void checkUnafk(Player plr){
        if(!plr.equals(player)) return;

        user.setAfk(false);

        Component userMsg = Component.translatable("unafk.self")
                .color(NamedTextColor.GRAY);

        Component broadcastMsg = Component.translatable("unafk.others", user.nickDisplayName()).color(NamedTextColor.GRAY);


        user.sendMessage(userMsg);
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(user.getUniqueId()))
                .forEach(p -> p.sendMessage(broadcastMsg));

        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!event.getPlayer().equals(player)) return;
        if(!event.hasChangedBlock()) return;

        checkUnafk(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        checkUnafk(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(event.getMessage().startsWith("/afk") || event.getMessage().startsWith("afk")) return;
        checkUnafk(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        checkUnafk(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        checkUnafk(event.getEntity());
    }
}
