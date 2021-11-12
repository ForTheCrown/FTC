package net.forthecrown.events;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
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
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.emotes.EmoteSmooch;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcFlags;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.chat.BannedWords;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.npc.NpcDirectory;
import net.forthecrown.economy.selling.SellShops;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.useables.kits.Kit;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.actions.MarriageMessage;
import net.forthecrown.user.actions.UserActionHandler;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.packets.PacketListeners;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;

import static net.forthecrown.core.chat.FtcFormatter.checkUppercase;
import static net.forthecrown.core.chat.FtcFormatter.formatColorCodes;

public class CoreListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event){
        CrownUser user = UserManager.getUser(event.getPlayer());
        boolean nameChanged = user.onJoin();

        PacketListeners.inject(event.getPlayer());

        if(!event.getPlayer().hasPlayedBefore()){
            event.getPlayer().teleport(Crown.getServerSpawn());

            Component welcomeMsg = Component.translatable("user.firstJoin", NamedTextColor.YELLOW, user.nickDisplayName());
            Crown.getAnnouncer().announceRaw(welcomeMsg);
            event.joinMessage(null);

            //Give join kit
            Kit kit = Crown.getKitManager().get(ComVars.onFirstJoinKit());
            if(kit != null) kit.attemptItemGiving(event.getPlayer());
        } else {
            user.sendMessage(Component.translatable("server.welcomeBack").color(NamedTextColor.GOLD));

            if(user.isVanished()) event.joinMessage(null);
            else event.joinMessage(nameChanged ? FtcFormatter.newNameJoinMessage(user) : FtcFormatter.joinMessage(user));
        }

        //Pirates.getParrotTracker().check(user.getPlayer());

        UserManager.updateVanishedFromPerspective(user);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), user::onJoinLater, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event){
        CrownUser user = UserManager.getUser(event.getPlayer());
        user.onLeave();

        PacketListeners.unInject(event.getPlayer());

        //Pirates.getParrotTracker().check(user.getPlayer());

        if(user.isVanished()) event.quitMessage(null);
        else event.quitMessage(FtcFormatter.leaveMessage(user));
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
        if(ComVars.getHoppersInOneChunk() == -1) return;
        int hopperAmount = event.getBlock().getChunk().getTileEntities(block -> block.getType() == Material.HOPPER, true).size();
        if(hopperAmount <= ComVars.getHoppersInOneChunk()) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Too many hoppers (Max " + ComVars.getHoppersInOneChunk() + ")").color(NamedTextColor.RED));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        event.line(0, FtcFormatter.formatIfAllowed(event.getLine(0), player));
        event.line(1, FtcFormatter.formatIfAllowed(event.getLine(1), player));
        event.line(2, FtcFormatter.formatIfAllowed(event.getLine(2), player));
        event.line(3, FtcFormatter.formatIfAllowed(event.getLine(3), player));

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
        if(!RoyalWeapons.isRoyalSword(item)) return;

        RoyalSword sword = new RoyalSword(item);
        if(!ComVars.allowNonOwnerSwords() && !sword.getOwner().equals(player.getUniqueId())) return;

        sword.kill(player, event.getEntity());
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

        if(!query.testState(weLoc, wgPlayer, FtcFlags.TRAPDOOR_USE) && !canBypass){
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
        event.renderer(new FtcChatRenderer());

        //Check to make sure no bad bad's were said
        Component rendered = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), event.getPlayer());
        if(BannedWords.checkAndWarn(event.getPlayer(), rendered)) {
            EavesDropper.bannedWordChat(rendered);

            event.setCancelled(true);
            return;
        }

        PunishmentManager punishments = Crown.getPunishmentManager();
        Player player = event.getPlayer();
        MuteStatus status = punishments.checkMute(player);

        if(status != MuteStatus.NONE){
            event.viewers().removeIf(a -> {
                Player p = fromAudience(a);
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
                Player p = fromAudience(a);
                if(p == null) return false;

                return  !p.hasPermission(Permissions.STAFF_CHAT) || StaffChat.ignoring.contains(p);
            });
            return;
        }

        if(player.getWorld().equals(Worlds.SENATE)){
            event.viewers().removeIf(a -> {
                Player p = fromAudience(a);
                if(p == null) return false;

                return !p.getWorld().equals(Worlds.SENATE);
            });
            return;
        } else event.viewers().removeIf(a -> {
            Player p = fromAudience(a);
            if(p == null) return false;

            return p.getWorld().equals(Worlds.SENATE);
        });

        //Remove ignored
        event.viewers().removeIf(a -> {
            Player p = fromAudience(a);
            if(p == null) return false;

            UserInteractions inter = UserManager.getUser(p).getInteractions();

            return inter.isBlockedPlayer(player.getUniqueId());
        });

        CrownUser user = UserManager.getUser(player);
        UserInteractions inter = user.getInteractions();

        if(inter.mChatToggled()){
            event.viewers().clear();

            if(inter.getSpouse() == null){
                user.sendMessage(
                        Component.text()
                                .append(Component.translatable("marriage.notMarried").color(NamedTextColor.RED))
                                .append(Component.newline())
                                .append(Component.text("How do you even have marriage chat enabled lol").color(NamedTextColor.GRAY))
                );

                inter.setMChatToggled(false);
                return;
            }

            CrownUser target = UserManager.getUser(inter.getSpouse());
            if(!target.isOnline()){
                user.sendMessage(
                        Component.translatable(UserArgument.USER_NOT_ONLINE.getTranslationKey(), target.nickDisplayName())
                                .color(NamedTextColor.RED)
                );
                return;
            }

            MarriageMessage message = new MarriageMessage(user, UserManager.getUser(inter.getSpouse()), ChatUtils.getString(event.message()));
            UserActionHandler.handleAction(message);
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

    private final CrownRandom random = new CrownRandom();

    @EventHandler(ignoreCancelled = true)
    public void onPaperServerListPing(PaperServerListPingEvent event) {
        int max = Bukkit.getMaxPlayers();
        int newMax = random.intInRange(max, max + 20);

        event.setMaxPlayers(newMax);

        event.motd(motd());

        Iterator<Player> iterator = event.iterator();
        while (iterator.hasNext()) {
            CrownUser user = UserManager.getUser(iterator.next());
            if(user.isVanished()) iterator.remove();
        }
    }

    Component motd() {
        return Component.text()
                .color(NamedTextColor.GRAY)

                .append(Component.text("For The Crown").style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)))
                .append(Component.text(" - "))
                .append(afterDashText())

                .append(Component.newline())
                .append(Component.text("Currently on " + Bukkit.getMinecraftVersion()))

                .build();
    }

    Component afterDashText() {
        if(Crown.inDebugMode()) return Component.text("Test server").color(NamedTextColor.GREEN);
        if(Bukkit.hasWhitelist()) return Component.text("Maintenance").color(NamedTextColor.RED);

        if(random.nextInt(50) == 45) return Component.text("You're amazing ")
                .append(EmoteSmooch.HEART)
                .color(NamedTextColor.RED);

        return Component.text("Survival Minecraft").color(NamedTextColor.YELLOW);
    }

    //Gets a player from an audience, used by the chat event listener in CoreListener
    static @Nullable Player fromAudience(Audience audience){
        if(audience instanceof Player) return (Player) audience;
        return null;
    }

    public static class FtcChatRenderer implements ChatRenderer {
        private Component message;

        @Override
        public @NotNull Component render(@NotNull Player source, @NotNull Component displayName, @NotNull Component message, @NotNull Audience viewer) {
            return Objects.requireNonNullElse(this.message, this.message = format(source, message));
        }

        private Component format(Player source, Component message) {
            CrownUser user = UserManager.getUser(source);

            String strMessage = ChatUtils.getString(message);
            boolean inSenateWorld = source.getWorld().equals(Worlds.SENATE);
            boolean staffChat = StaffChat.toggledPlayers.contains(source);

            if(source.hasPermission(Permissions.DONATOR_2) || inSenateWorld || staffChat) strMessage = formatColorCodes(strMessage);
            if(source.hasPermission(Permissions.DONATOR_3) || inSenateWorld || staffChat) strMessage = Crown.getEmotes().format(strMessage, source, true);

            strMessage = checkUppercase(source, strMessage);
            message = ChatUtils.convertString(strMessage);

            TextColor playerColor = inSenateWorld ? NamedTextColor.YELLOW : TextColor.color(240, 240, 240);
            if(staffChat) playerColor = NamedTextColor.GRAY;

            return Component.text()
                    .append(StaffChat.toggledPlayers.contains(source) ? StaffChat.PREFIX : Component.empty())
                    .append(user.nickDisplayName().color(playerColor))
                    .append(Component.text(" > ").style(
                            Style.style(NamedTextColor.GRAY, TextDecoration.BOLD)
                    ))
                    .append(message)
                    .build();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getKeepInventory()) return;

        CrownUser user = UserManager.getUser(event.getEntity());
        org.bukkit.Location loc = user.getLocation();

        user.setLastLocation(loc);

        // Tell the Player where they died, but ignore world_void deaths.
        String diedAt = "died at x=" + loc.getBlockX() + ", y=" + loc.getBlockY() + ", z=" + loc.getBlockZ() + ".";
        if (!loc.getWorld().getName().equalsIgnoreCase("world_void"))
            user.sendMessage(ChatColor.GRAY + "[FTC] You " + diedAt);

        Announcer.log(Level.INFO, "! " + user.getName() + " " + diedAt);

        /*Grave grave = user.getGrave();
        for (ItemStack i: event.getEntity().getInventory()){
            if(FtcItems.isCrownItem(i)){
                grave.addItem(i);
                event.getDrops().remove(i);
            }
        }*/

        PlayerInventory inventory = event.getEntity().getInventory();
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if(FtcUtils.isItemEmpty(item)) continue;

            if(FtcItems.isCrownItem(item) || RoyalWeapons.isRoyalSword(item)) {
                items.put(i, item);;
            }
        }
        event.getDrops().removeAll(items.values());

        Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
            PlayerInventory inv = user.getPlayer().getInventory();

            for (Int2ObjectMap.Entry<ItemStack> e: items.int2ObjectEntrySet()) {
                inv.setItem(e.getIntKey(), e.getValue());
            }
        }, 1);
    }
}
