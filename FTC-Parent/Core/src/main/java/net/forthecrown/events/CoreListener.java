package net.forthecrown.events;

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
import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.WgFlags;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.npc.NpcDirectory;
import net.forthecrown.economy.selling.SellShops;
import net.forthecrown.inventory.CrownWeapons;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.useables.kits.Kit;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.data.MarriageMessage;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CoreListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        CrownUser user = UserManager.getUser(event.getPlayer());
        boolean nameChanged = user.onJoin();

        if(!event.getPlayer().hasPlayedBefore()){
            user.getPlayer().teleport(ForTheCrown.getServerSpawn());

            Component welcomeMsg = Component.translatable("user.firstJoin", NamedTextColor.YELLOW, user.nickDisplayName());
            ForTheCrown.getAnnouncer().announceRaw(welcomeMsg);
            event.joinMessage(null);

            //Give join kit
            Kit kit = ForTheCrown.getKitRegistry().get(ForTheCrown.onFirstJoinKit());
            if(kit != null) kit.attemptItemGiving(event.getPlayer());
        } else {
            user.sendMessage(Component.translatable("server.welcomeBack").color(NamedTextColor.GOLD));

            if(user.isVanished()) event.joinMessage(null);
            else event.joinMessage(nameChanged ? ChatFormatter.newNameJoinMessage(user) : ChatFormatter.joinMessage(user));
        }

        Pirates.getParrotTracker().check(user.getPlayer());

        UserManager.updateVanishedFromPerspective(user);
        Bukkit.getScheduler().scheduleSyncDelayedTask(ForTheCrown.inst(), user::onJoinLater, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event){
        CrownUser user = UserManager.getUser(event.getPlayer());
        user.onLeave();

        Pirates.getParrotTracker().check(user.getPlayer());

        if(user.isVanished()) event.quitMessage(null);
        else event.quitMessage(ChatFormatter.formatLeaveMessage(user));
    }

    @EventHandler
    public void onServerShopNpcUse(PlayerInteractEntityEvent event) {
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getRightClicked().getType() != EntityType.WANDERING_TRADER) return;
        LivingEntity trader = (LivingEntity) event.getRightClicked();

        if(trader.hasAI() && trader.getCustomName() == null || !trader.getCustomName().contains("Server Shop")) return;

        SellShops.MAIN.open(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getBlock().getType() != Material.HOPPER) return;
        if(ForTheCrown.getHoppersInOneChunk() == -1) return;
        int hopperAmount = event.getBlock().getChunk().getTileEntities(block -> block.getType() == Material.HOPPER, true).size();
        if(hopperAmount <= ForTheCrown.getHoppersInOneChunk()) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Too many hoppers (Max " + ForTheCrown.getHoppersInOneChunk() + ")").color(NamedTextColor.RED));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        event.line(0, ChatFormatter.formatIfAllowed(event.getLine(0), player));
        event.line(1, ChatFormatter.formatIfAllowed(event.getLine(1), player));
        event.line(2, ChatFormatter.formatIfAllowed(event.getLine(2), player));
        event.line(3, ChatFormatter.formatIfAllowed(event.getLine(3), player));

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

        if(!query.testState(weLoc, wgPlayer, WgFlags.TRAPDOOR_USE) && !canBypass){
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

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        event.renderer(ChatFormatter::formatChat);

        PunishmentManager punishments = ForTheCrown.getPunishmentManager();
        Player player = event.getPlayer();
        MuteStatus status = punishments.checkMute(player);

        if(status != MuteStatus.NONE){
            event.viewers().removeIf(a -> {
                Player p = FtcUtils.fromAudience(a);
                if(p == null) return false;

                return !punishments.isSoftmuted(p.getUniqueId());
            });
            EavesDropper.reportMuted(event.message(), player, status);

            if(status == MuteStatus.HARD) event.setCancelled(true); //Completely cancel if they are hardmuted, checkMute sends them mute message
            return;
        }

        if (StaffChat.toggledPlayers.contains(player)) {
            if(StaffChat.ignoring.contains(player)){
                player.sendMessage(Component.text("You are ignoring staff chat, do '/sct visible' to use it again").color(NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            event.viewers().removeIf(a -> {
                Player p = FtcUtils.fromAudience(a);
                if(p == null) return false;

                return  !p.hasPermission(Permissions.STAFF_CHAT) || StaffChat.ignoring.contains(p);
            });
            return;
        }

        if(player.getWorld().equals(Worlds.SENATE)){
            event.viewers().removeIf(a -> {
                Player p = FtcUtils.fromAudience(a);
                if(p == null) return false;

                return !p.getWorld().equals(Worlds.SENATE);
            });
            return;
        } else event.viewers().removeIf(a -> {
            Player p = FtcUtils.fromAudience(a);
            if(p == null) return false;

            return p.getWorld().equals(Worlds.SENATE);
        });

        //Remove ignored
        event.viewers().removeIf(a -> {
            Player p = FtcUtils.fromAudience(a);
            if(p == null) return false;

            UserInteractions inter = UserManager.getUser(p).getInteractions();

            return inter.isBlockedPlayer(player.getUniqueId());
        });

        CrownUser user = UserManager.getUser(player);
        UserInteractions inter = user.getInteractions();

        if(inter.mChatToggled()){
            event.viewers().clear();

            if(inter.getMarriedTo() == null){
                user.sendMessage(
                        Component.text()
                                .append(Component.translatable("marriage.notMarried").color(NamedTextColor.RED))
                                .append(Component.newline())
                                .append(Component.text("How do you even have marriage chat enabled lol").color(NamedTextColor.GRAY))
                );

                inter.setMChatToggled(false);
                return;
            }

            CrownUser target = UserManager.getUser(inter.getMarriedTo());
            if(!target.isOnline()){
                user.sendMessage(
                        Component.translatable(UserType.USER_NOT_ONLINE.getTranslationKey(), target.nickDisplayName())
                                .color(NamedTextColor.RED)
                );
                return;
            }

            new MarriageMessage(user, UserManager.getUser(inter.getMarriedTo()), ChatUtils.getString(event.message()))
                    .complete();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!event.getRightClicked().getPersistentDataContainer().has(NpcDirectory.KEY, PersistentDataType.STRING)) return;

        event.setCancelled(true);
        NpcDirectory.interact(
                event.getRightClicked().getPersistentDataContainer().get(NpcDirectory.KEY, PersistentDataType.STRING),
                event.getRightClicked(),
                event.getPlayer()
        );
    }
}
