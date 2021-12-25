package net.forthecrown.inventory.weapon;

import net.forthecrown.inventory.weapon.click.ClickHistory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public abstract class AltAttackContext extends WeaponContext {
    public final EquipmentSlot hand;

    public AltAttackContext(Player player, RoyalSword sword, EquipmentSlot hand, ItemStack item, ClickHistory history) {
        super(player, item, sword, history);
        this.hand = hand;
    }

    public static class c_Block extends AltAttackContext {
        public final Block block;
        public final BlockFace blockFace;

        public c_Block(PlayerInteractEvent event, RoyalSword sword, ClickHistory history) {
            super(event.getPlayer(), sword, event.getHand(), event.getItem(), history);

            this.block = event.getClickedBlock();
            this.blockFace = event.getBlockFace();
        }
    }

    public static class c_Entity extends AltAttackContext {
        public final Entity entity;

        public c_Entity(PlayerInteractEntityEvent event, RoyalSword sword, ClickHistory history) {
            super(event.getPlayer(), sword, event.getHand(), event.getPlayer().getInventory().getItemInMainHand(), history);

            this.entity = event.getRightClicked();
        }
    }
}
