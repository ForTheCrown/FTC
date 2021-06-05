package net.forthecrown.emperor.commands.emotes;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EmoteScare extends CommandEmote {

    public EmoteScare(){
        super("scare", 30*20, ChatColor.GRAY + "You scare people too often lol" + "\n" + ChatColor.DARK_GRAY + "This only works every 30 seconds.");

        setDescription("description: Scares another player.");
        register();
    }

    @Override
    protected int execute(CrownUser sender, CrownUser recipient) {
        sender.sendMessage(
                Component.text("You scared ")
                        .append(recipient.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(Component.text("!"))
        );

        recipient.sendMessage(
                Component.text()
                        .append(sender.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(Component.text(" scared you!"))

                        .clickEvent(ClickEvent.runCommand("/scare " + sender.getName()))
                        .hoverEvent(Component.text("Scare them back! :D"))

                        .build()
        );

        scare(recipient.getPlayer());
        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        scare(user.getPlayer());
        return 0;
    }

    private void scare(Player player) {
        Location loc = player.getLocation();
        player.spawnParticle(Particle.MOB_APPEARANCE, loc.getX(), loc.getY(), loc.getZ(), 1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CrownCore.inst(), () -> {
            player.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 2.0F, 1F);

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false, false));

            for (int i = 0; i < 3; i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(CrownCore.inst(), () -> player.playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.MASTER, 1.5F, 1F), i* 3L);
            }
        }, 3L);
    }
}
