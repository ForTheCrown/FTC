package net.forthecrown.julyevent.effects;

import net.forthecrown.julyevent.JulyMain;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Supplier;

public class PotionProvider implements Supplier<PotionEffect>, BlockEffect {

    private final PotionEffectType type;

    public PotionProvider(PotionEffectType type) {
        this.type = type;
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
        player.addPotionEffect(get());
    }
}
