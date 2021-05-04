package net.forthecrown.mayevent;

import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockDamageTracker {
    public static final Map<Block, Byte> DAMAGED = new HashMap<>();

    public static byte damage(Block block, double damage){
        return damage(block, (byte) damage);
    }

    public static byte getDamage(Block block){
        return DAMAGED.getOrDefault(block, (byte) 0);
    }

    public static byte damage(Block block, byte damage){
        if(block == null) return -1;
        if(MayUtils.isNonDestructable(block.getType())) return -1;

        if(DAMAGED.containsKey(block)){
            damage += getDamage(block);

            if(damage > 30) {
                DAMAGED.remove(block);
                MayUtils.breakBlock(block);
                return 31;
            }
        }
        DAMAGED.put(block, damage);
        return damage;
    }

    public static void clear(){ DAMAGED.clear(); }

    private BlockDamageTracker() {}
}
