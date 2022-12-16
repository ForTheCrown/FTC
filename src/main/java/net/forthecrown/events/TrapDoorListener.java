package net.forthecrown.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.forthecrown.core.FtcFlags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class TrapDoorListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        BlockData data = event.getClickedBlock().getBlockData();

        if (!(data instanceof TrapDoor)) {
            return;
        }

        com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(block.getLocation());
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());

        WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
        RegionQuery query = platform.getRegionContainer().createQuery();
        boolean canBypass = platform.getSessionManager().hasBypass(wgPlayer, BukkitAdapter.adapt(block.getWorld()));

        if (!query.testState(weLoc, wgPlayer, FtcFlags.TRAPDOOR_USE)
                && !canBypass
        ) {
            ApplicableRegionSet set = platform.getRegionContainer()
                    .get(BukkitAdapter.adapt(block.getWorld()))
                    .getApplicableRegions(BlockVector3.at(weLoc.getBlockX(), weLoc.getBlockY(), weLoc.getBlockZ()));

            for (ProtectedRegion region: set) {
                if (region.isMember(wgPlayer)) {
                    return;
                }
            }

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