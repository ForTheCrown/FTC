package net.forthecrown.commands.emotes;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EmoteScare extends CommandEmote {

    public EmoteScare(){
        super("scare", 30*20);

        setDescription("description: Scares another player.");
        setPermission(Permissions.EMOTE_SCARE);

        register();
    }

    @Override
    public int execute(CrownUser sender, CrownUser target) {
        sender.sendMessage(
                Component.translatable("emotes.scare.sender", target.nickDisplayName().color(NamedTextColor.YELLOW))
        );

        target.sendMessage(
                Component.translatable("emotes.scare.target", sender.nickDisplayName().color(NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.runCommand("/" + getName() + " " + sender.getName()))
                        .hoverEvent(Component.translatable("emotes.scare.target.hover"))
        );

        scare(target.getPlayer());
        return 0;
    }

    @Override
    public int executeSelf(CrownUser user) {
        scare(user.getPlayer());
        return 0;
    }

    private void scare(Player player) {
        Location loc = player.getLocation();
        player.spawnParticle(Particle.MOB_APPEARANCE, loc.getX(), loc.getY(), loc.getZ(), 1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> {
            player.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 2.0F, 1F);

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false, false));

            for (int i = 0; i < 3; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> player.playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.MASTER, 1.5F, 1F), i* 3L);
            }
        }, 3L);
    }
}
