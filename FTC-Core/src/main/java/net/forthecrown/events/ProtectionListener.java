package net.forthecrown.events;

import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.protection.ClaimVisualizer;
import net.forthecrown.protection.Claims;
import net.forthecrown.protection.ProtectedClaim;
import net.forthecrown.protection.UserClaimSession;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ProtectionListener implements Listener {
    private final Claims claims;

    public ProtectionListener() {
        this.claims = Objects.requireNonNull(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if(!claims.allowsClaims(world)) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if(ItemStacks.isEmpty(held)) return;

        Vector3i clicked = Vector3i.of(event.getClickedBlock());

        if(held.getType() == Material.STICK) {
            ProtectedClaim claim = claims.getClaimMap(world).get(clicked);
            if(claim == null) return;

            ClaimVisualizer.Context context = new ClaimVisualizer.Context(world, player, claim)
                    .setErrorType(false)
                    .setIncludeChildren(true);

            ClaimVisualizer.visualize(context);
            return;
        }

        if(held.getType() == Material.GOLDEN_SHOVEL) {
            CrownUser user = UserManager.getUser(player);
            UserClaimSession session = user.getClaimSession();
            if(session == null) return;

            //????
        }
    }
}
