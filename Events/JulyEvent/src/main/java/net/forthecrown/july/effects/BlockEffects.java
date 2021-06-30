package net.forthecrown.july.effects;

import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class BlockEffects {

    private static final Map<Material, BlockEffect> BLOCK_EFFECTS = new HashMap<>(4);

    public static void init(){
        register(new JumpBlockEffect());

        register(new ItemBlockEvent(
                new ItemStackBuilder(Material.ELYTRA)
                        .setUnbreakable(true)
                        .build(),
                EquipmentSlot.CHEST
        ));

        register(
                new PotionBlockEffect(
                        PotionEffectType.SLOW,
                        Sound.sound(
                                Key.key("block.conduit.deactivate"),
                                Sound.Source.PLAYER,
                                1f, 1f
                        ),
                        Material.BLACK_GLAZED_TERRACOTTA
                )
        );

        register(
                new PotionBlockEffect(
                        PotionEffectType.SPEED,
                        Sound.sound(
                                Key.key("block.conduit.activate"),
                                Sound.Source.PLAYER,
                                1f, 1f
                        ),
                        Material.MAGENTA_GLAZED_TERRACOTTA
                )
        );
    }

    public static void register(BlockEffect effect){
        BLOCK_EFFECTS.put(effect.getMaterial(), effect);
    }

    public static void attemptExecution(Player player, Material standingOn){
        if(Cooldown.contains(player, "Event_MoveCooldown")) return;

        BlockEffect effect = BLOCK_EFFECTS.get(standingOn);
        if(effect == null) return;

        Cooldown.add(player, "Event_MoveCooldown", 30);
        effect.apply(player);
    }

    public static Map<Material, BlockEffect> getBlockEffects() {
        return BLOCK_EFFECTS;
    }
}
