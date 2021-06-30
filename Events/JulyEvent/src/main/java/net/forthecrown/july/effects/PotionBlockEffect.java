package net.forthecrown.july.effects;

import net.forthecrown.july.EventUtils;
import net.forthecrown.july.JulyMain;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Supplier;

public class PotionBlockEffect implements Supplier<PotionEffect>, BlockEffect {

    private final PotionEffectType type;
    private final Material material;
    private final Sound sound;

    public PotionBlockEffect(PotionEffectType type, Sound sound, Material material) {
        this.type = type;
        this.material = material;
        this.sound = sound;
    }

    public PotionEffectType getType() {
        return type;
    }

    @Override
    public PotionEffect get(){
        return new PotionEffect(type,
                JulyMain.potionDuration(),
                JulyMain.potionAmplifier(),
                true,
                false
        );
    }

    @Override
    public void apply(Player player) {
        player.playSound(sound);
        EventUtils.clearEffects(player);
        player.addPotionEffect(get());
    }

    @Override
    public Material getMaterial() {
        return material;
    }

}
