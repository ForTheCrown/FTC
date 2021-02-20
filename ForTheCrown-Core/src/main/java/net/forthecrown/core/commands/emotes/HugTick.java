package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import org.bukkit.Bukkit;
import org.bukkit.Particle;

public class HugTick implements Runnable{
    private int i = 0;
    private final int id;
    private final CrownUser user;

    public HugTick(CrownUser user){
        this.user = user;

        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(FtcCore.getInstance(), this, 0, 2);
    }

    @Override
    public void run() {
        if(i == 10*10){
            Bukkit.getScheduler().cancelTask(id);
            Cooldown.remove(user, "Emote_Hug_Received");
            return;
        }

        user.getPlayer().getWorld().spawnParticle(Particle.HEART, user.getPlayer().getLocation().clone().add(0, 1, 0), 1, 0.25, 0.25 ,0.25);
        i++;
    }
}
