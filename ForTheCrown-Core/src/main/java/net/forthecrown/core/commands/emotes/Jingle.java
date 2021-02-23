package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.EmoteDisabledException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Jingle extends CrownCommand {

    public Jingle(){
        super("jingle", FtcCore.getInstance());

        setPermission("ftc.emotes.jingle");
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
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command.");
            return false;
        }
        Player player = (Player) sender;
        CrownUser playerData = FtcCore.getUser(player.getUniqueId());

        if(Cooldown.contains(player, "Core_Emote_Jingle")) {
            sender.sendMessage(ChatColor.GRAY + "You jingle too often lol");
            sender.sendMessage(ChatColor.DARK_GRAY + "This only works every 6 seconds.");
            return false;
        }

        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) {
            Cooldown.add(player, "Core_Emote_Jingle" , 6*20);
            jingle(player);
            return true;
        }
        if(!playerData.allowsEmotes()) throw new EmoteDisabledException(sender).senderDisabled();

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) throw new InvalidPlayerInArgument(sender, args[0]);

        CrownUser targetData = FtcCore.getUser(target.getUniqueId());

        if(!targetData.allowsEmotes()){
            player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
            return false;
        }

        // Actual jingling:
        player.sendMessage("You've sent " + ChatColor.YELLOW + target.getName() + ChatColor.RESET + " a sick Christmas beat!");
        target.sendMessage("You've received jingle vibes from " + ChatColor.YELLOW + player.getName() + ChatColor.RESET + "!");

        Cooldown.add(player, "Core_Emote_Jingle" , 6*20);
        jingle(target);

        return true;
    }

    private void jingle(Player player) {
        Location loc = player.getLocation();
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
