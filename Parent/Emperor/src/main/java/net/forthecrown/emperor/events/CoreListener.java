package net.forthecrown.emperor.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.forthecrown.emperor.Announcer;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.CrownWorldGuard;
import net.forthecrown.emperor.admin.EavesDropper;
import net.forthecrown.emperor.admin.PunishmentEntry;
import net.forthecrown.emperor.admin.PunishmentManager;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.economy.SellShop;
import net.forthecrown.emperor.inventory.CrownWeapons;
import net.forthecrown.emperor.useables.kits.Kit;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CoreListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        CrownUser user = UserManager.getUser(event.getPlayer());
        user.onJoin();

        if(!event.getPlayer().hasPlayedBefore() || CrownCore.inDebugMode()){
            user.getPlayer().teleport(CrownCore.getServerSpawn());

            Component welcomeMsg = Component.translatable("user.firstJoin", user.nickDisplayName())
                    .hoverEvent(Component.text("Click to welcome them!"))
                    .clickEvent(ClickEvent.runCommand("Welcome " + user.getNickOrName() + '!'))
                    .color(NamedTextColor.YELLOW);

            CrownCore.getAnnouncer().announceRaw(welcomeMsg);

            //Give join kit
            Kit kit = CrownCore.getKitRegistry().get(CrownCore.onFirstJoinKit());
            if(kit != null) kit.attemptItemGiving(event.getPlayer());
        } else user.sendMessage(Component.translatable("server.welcomeBack").color(NamedTextColor.GOLD));

        if(user.isVanished()) event.joinMessage(null);
        else event.joinMessage(ChatFormatter.joinMessage(user));

        UserManager.updateVanishedFromPerspective(user);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CrownCore.inst(), user::onJoinLater, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event){
        CrownUser user = UserManager.getUser(event.getPlayer());
        user.onLeave();

        if(user.isVanished()) event.quitMessage(null);
        else event.quitMessage(ChatFormatter.formatLeaveMessage(user));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Announcer.debug(1);
        PunishmentManager manager = CrownCore.getPunishmentManager();
        PunishmentEntry entry = manager.getEntry(event.getUniqueId());

        if(entry == null) return;
        if(entry.checkPunished(PunishmentType.BAN)){
            Announcer.debug(2);
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    ChatFormatter.banMessage(entry.getCurrent(PunishmentType.BAN))
            );
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        }
        Announcer.debug(3);
    }

    @EventHandler
    public void onServerShopNpcUse(PlayerInteractEntityEvent event){
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getRightClicked().getType() != EntityType.WANDERING_TRADER) return;
        LivingEntity trader = (LivingEntity) event.getRightClicked();

        if(trader.hasAI() && trader.getCustomName() == null || !trader.getCustomName().contains("Server Shop")) return;

        event.getPlayer().openInventory(new SellShop(event.getPlayer()).mainMenu());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getBlock().getType() != Material.HOPPER) return;
        if(CrownCore.getHoppersInOneChunk() == -1) return;
        int hopperAmount = event.getBlock().getChunk().getTileEntities(block -> block.getType() == Material.HOPPER, true).size();
        if(hopperAmount <= CrownCore.getHoppersInOneChunk()) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Too many hoppers (Max " + CrownCore.getHoppersInOneChunk() + ")").color(NamedTextColor.RED));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        event.line(0, ChatFormatter.formatStringIfAllowed(event.getLine(0), player));
        event.line(1, ChatFormatter.formatStringIfAllowed(event.getLine(1), player));
        event.line(2, ChatFormatter.formatStringIfAllowed(event.getLine(2), player));
        event.line(3, ChatFormatter.formatStringIfAllowed(event.getLine(3), player));

        EavesDropper.reportSignPlacement(event.getPlayer(), event.getBlock().getLocation(),
                event.line(0),
                event.line(1),
                event.line(2),
                event.line(3)
        );
    }

    //Entity death by crown weapon
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR) return;

        if(!CrownWeapons.isLegacyWeapon(item) && !CrownWeapons.isCrownWeapon(item)) return;
        EntityDamageEvent event2 = event.getEntity().getLastDamageCause();
        if (!(event2.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || event2.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK))) return;

        CrownWeapons.Weapon weapon = CrownWeapons.fromItem(item);

        if(weapon.getTarget() == EntityType.AREA_EFFECT_CLOUD){
            if(!(event.getEntity() instanceof Creeper)) return;
            if(!((Creeper) event.getEntity()).isPowered()) return;
        } else if(event.getEntity().getType() != weapon.getTarget()) return;

        short pog = (short) (weapon.getProgress() + 1);
        if(pog >= weapon.getGoal()) CrownWeapons.upgradeLevel(weapon, player);
        else {
            weapon.setProgress(pog);
            weapon.update();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        BlockData data = event.getClickedBlock().getBlockData();
        if(!(data instanceof TrapDoor)) return;

        Location weLoc = BukkitAdapter.adapt(block.getLocation());
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());

        WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
        RegionQuery query = platform.getRegionContainer().createQuery();
        boolean canBypass = platform.getSessionManager().hasBypass(wgPlayer, BukkitAdapter.adapt(block.getWorld()));

        if(!query.testState(weLoc, wgPlayer, CrownWorldGuard.TRAPDOOR_USE) && !canBypass){
            ApplicableRegionSet set = platform.getRegionContainer().get(BukkitAdapter.adapt(block.getWorld())).getApplicableRegions(BlockVector3.at(weLoc.getBlockX(), weLoc.getBlockY(), weLoc.getBlockZ()));

            for (ProtectedRegion region: set) if(region.isMember(wgPlayer)) return;

            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    Component.text()
                            .append(Component.text("Hey!").style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)))
                            .append(Component.text(" You can't do that here!").color(NamedTextColor.GRAY))
                            .build()
            );
        }
    }
}
