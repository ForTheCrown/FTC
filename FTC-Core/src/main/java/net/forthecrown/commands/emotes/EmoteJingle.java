package net.forthecrown.commands.emotes;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;

import java.util.Arrays;

public class EmoteJingle extends CommandEmote {

    public EmoteJingle(){
        super("jingle", 6*20);

        setDescription("Plays a christmas song. :)");
        setPermission(Permissions.EMOTE_JINGLE);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Command that allows players to vibe on Jingle Bells.
     * Only works if they both have emotes enabled.
     *
     * Valid usages of command:
     * - /jingle
     *
     * Main Author: Wout
     * Edit: Julie
     */

    @Override
    public int execute(CrownUser sender, CrownUser target) {
        sender.sendMessage(Component.translatable("emotes.jingle.sender", target.nickDisplayName()));

        target.sendMessage(
                Component.translatable("emotes.jingle.target", sender.nickDisplayName().color(NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.runCommand("/" + getName() + " " + sender.getName()))
                        .hoverEvent(Component.translatable("emotes.jingle.target.hover"))
        );

        jingle(target);
        return 0;
    }

    @Override
    public int executeSelf(CrownUser user) {
        Cooldown.add(user, cooldownCategory, cooldownTime);
        jingle(user);
        return 0;
    }

    //Jingle, by the illustrious composer Woutzart xD
    private void jingle(CrownUser user) {
        Location loc = user.getPlayer().getLocation();
        loc.getWorld().spawnParticle(Particle.SNOW_SHOVEL,loc, 25, 0.1, 0, 0.1, 1);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 0.1, 0, 0.1, 0.1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> {
            //b = bass //s = snare
            playSound(0, loc, midTone); //b
            playSound(4, loc, midTone); //s
            playSound(8, loc, midTone); //b

            playSound(16, loc, midTone); //b
            playSound(20, loc, midTone); //s
            playSound(24, loc, midTone); //b

            playSound(32, loc, midTone); //b
            playSound(36, loc, 1.8f); //s
            playSound(40, loc, 1.2f); //b
            playSound(44, loc, midTone); //s

            playSound(48, loc, midTone); //b
            playSound(52, loc, midTone); //s
            playSound(56, loc, midTone); //b

            playSound(64, loc, highTone); //b
            playSound(68, loc, highTone); //s
            playSound(72, loc, highTone); //b

            playSound(78, loc, highTone); //s
            playSound(80, loc, highTone); //b
            playSound(84, loc, midTone); //s
            playSound(88, loc, midTone); //b

            playSound(96, loc, midTone);  //b
            playSound(100, loc, 1.3f); //s
            playSound(104, loc, 1.3f); //s
            playSound(108, loc, 1.7f); //s
            playSound(112, loc, midTone); //s
            playSound(120, loc, 2.0f);
        }, 8);
    }

    private final IntSet bass = new IntOpenHashSet(Arrays.asList(
            0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96
    ));
    private final IntSet snare = new IntOpenHashSet(Arrays.asList(
            4, 20, 36, 44, 52, 68, 78, 84, 100, 104, 108, 112
    ));
    private final float midTone = 1.5f;
    private final float highTone = 1.7f;

    private void playSound(int delay, Location loc, float pitch) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> {
            if (bass.contains(delay)) {
                loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 0.2F, 1F);
            }
            else if (snare.contains(delay)) {
                loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.MASTER, 1F, 1F);
            }
            loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1F, pitch);
        }, delay);
    }
}