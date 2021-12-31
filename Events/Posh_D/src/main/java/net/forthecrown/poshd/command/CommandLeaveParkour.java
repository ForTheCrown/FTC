package net.forthecrown.poshd.command;

import net.forthecrown.crown.EventTimer;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.poshd.Main;
import net.forthecrown.poshd.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class CommandLeaveParkour extends AbstractCommand {
    public CommandLeaveParkour() {
        super("leaveparkour", Main.inst);

        setAliases("pleave", "leavep", "parkourleave");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = c.getSource().asPlayer();
            EventTimer timer = Main.TIMERS.get(player.getUniqueId());

            if(timer == null) {
                throw CommandCheckPoint.NOT_IN_EVENT.create();
            }

            if(!timer.wasStopped()) timer.stop();

            player.teleport(timer.exitLocation);
            player.sendMessage(Messages.leftEvent());
            clearEffects(player);
            leaveTeams(player);
            return 0;
        });
    }

    public static void clearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    public static void leaveTeams(Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "team leave " + player.getName());
    }
}
