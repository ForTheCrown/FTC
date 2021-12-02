package net.forthecrown.inventory.weapon;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public abstract class AltAttackContext {
    public final Player player;
    public final RoyalSword sword;
    public final EquipmentSlot hand;
    public final ItemStack item;
    public final Vector interactionPoint;

    public AltAttackContext(Player player, RoyalSword sword, EquipmentSlot hand, ItemStack item, Vector interactionPoint) {
        this.player = player;
        this.sword = sword;
        this.hand = hand;
        this.item = item;
        this.interactionPoint = interactionPoint;
    }

    public static class Block extends AltAttackContext {
        public final org.bukkit.block.Block block;
        public final BlockFace blockFace;

        public Block(PlayerInteractEvent event, RoyalSword sword) {
            super(event.getPlayer(), sword, event.getHand(), event.getItem(), event.getInteractionPoint().toVector());

            this.block = event.getClickedBlock();
            this.blockFace = event.getBlockFace();
        }
    }

    public static class Entity extends AltAttackContext {
        public final org.bukkit.entity.Entity entity;

        public Entity(PlayerInteractEntityEvent event, RoyalSword sword) {
            super(event.getPlayer(), sword, event.getHand(), event.getPlayer().getInventory().getItemInMainHand(), event.getRightClicked().getLocation().add(0, 1, 0).toVector());

            this.entity = event.getRightClicked();
        }
    }
}
