package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import org.bukkit.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EmoteJingle extends CommandEmote {

    public EmoteJingle(){
        super("jingle", 6*20, ChatColor.GRAY + "You jingle too often lol" + "\n" + ChatColor.DARK_GRAY + "This only works every 6 seconds.");

        setDescription("Plays a christmas song. :)");
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
     * Referenced other classes:
     * - FtcCore
     * - Chat
     *
     * Main Author: Wout
     * Edit: Botul
     */

    @Override
    protected int execute(CrownUser sender, CrownUser recipient) {
        sender.sendMessage("You've sent " + ChatColor.YELLOW + recipient.getName() + ChatColor.RESET + " a sick Christmas beat!");
        recipient.sendMessage("You've received jingle vibes from " + ChatColor.YELLOW + sender.getName() + ChatColor.RESET + "!");

        jingle(recipient);
        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        Cooldown.add(user, cooldownCategory, cooldownTime);
        jingle(user);
        return 0;
    }

    //Jingle, by the illustrious composer Woutzart xD
    private void jingle(CrownUser user) {
        Location loc = user.getPlayer().getLocation();
        loc.getWorld().spawnParticle(Particle.SNOW_SHOVEL,loc, 25, 0.1, 0, 0.1, 1);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 0.1, 0, 0.1, 0.1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), () -> {
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

    private final Set<Integer> bass = new HashSet<>(Arrays.asList(
            0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96));
    private final Set<Integer> snare = new HashSet<>(Arrays.asList(
            4, 20, 36, 44, 52, 68, 78, 84, 100, 104, 108, 112));
    private final float midTone = 1.5f;
    private final float highTone = 1.7f;

    private void playSound(int delay, Location loc, float pitch) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(FtcCore.getInstance(), () -> {
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
