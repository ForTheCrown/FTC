package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.api.CrownUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class EmoteBonk extends CommandEmote {

    public EmoteBonk(){
        super("bonk", 3*20, "&7You bonk people too often lol");

        setPermission("ftc.emotes");
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
        Location loc = recipient.getPlayer().getLocation();
        loc.setPitch(loc.getPitch() + 20F);

        sender.sendMessage("You bonked " + ChatColor.YELLOW + recipient.getName() + ChatColor.RESET + "!");
        recipient.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.RESET + " bonked you!");

        if(recipient.getPlayer().getGameMode() != GameMode.SPECTATOR){
            recipient.getPlayer().teleport(loc);
            recipient.getPlayer().getWorld().playSound(loc, Sound.ENTITY_SHULKER_HURT_CLOSED, 2.0F, 0.8F);
            recipient.getPlayer().getWorld().spawnParticle(Particle.CRIT, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        }
        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        user.sendMessage("Don't hurt yourself ❤");
        return 0;
    }
}
