package net.forthecrown.cosmetics.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.cosmetics.Cosmetics;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RemoveParticle extends CrownCommand {

    public RemoveParticle(){
        super("removeparticle", Cosmetics.plugin);

        setPermission("ftc.cosmetics.admin");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Removes a Particle from a player.
     *
     *
     * Valid usages of command:
     * - /removeparticle [player] [arrow/death/emote] [particle]
     *
     * Referenced other classes:
     * - Main: Main.plugin
     * - DataPlugin: configfile
     *
     * Author: Wout
     */


    @SuppressWarnings("deprecation")
    @Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 3) return false;

        CrownUser user;
        try {
            user = FtcCore.getUser(FtcCore.getOffOnUUID(args[0]));
        } catch (Exception e){ throw new InvalidPlayerInArgument(sender); }

        switch (args[1]) {
            case "arrow":
            {
                List<Particle> availableEffects = user.getParticleArrowAvailable();
                Particle part;
                try {
                    part = Particle.valueOf(args[2]);
                } catch (NullPointerException e){ throw new InvalidArgumentException(sender, "Not a valid particle"); }

                if (!Cosmetics.plugin.getAcceptedArrowParticles().contains(part)) {
                    sender.sendMessage(ChatColor.GRAY + "Use one of these particles:");
                    String message = ChatColor.GRAY + "";
                    for (Particle particle : Cosmetics.plugin.getAcceptedArrowParticles()) {
                        message += particle.toString() + ", ";
                    }
                    throw new InvalidArgumentException(sender, message);
                }
                if (availableEffects.contains(part)) {

                    availableEffects.remove(part);
                    user.setParticleArrowAvailable(availableEffects);

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Removed &f" + args[2] + "&7 from &f" + args[0] + "&7's arrow-particles."));
                    return true;
                }
                else throw new InvalidArgumentException(sender, "This player does not have this particle: " + args[2]);
            }
            case "death":
            {
                List<String> availableEffects = user.getParticleDeathAvailable();

                if (!Cosmetics.plugin.getAcceptedDeathParticles().contains(args[2])) {
                    sender.sendMessage(ChatColor.GRAY + "Use one of these particles:");
                    String message = ChatColor.GRAY + "";
                    for (String particle : Cosmetics.plugin.getAcceptedDeathParticles()) {
                        message += particle + ", ";
                    }
                    throw new InvalidArgumentException(sender, message);
                }
                if (availableEffects.contains(args[2])) {
                    availableEffects.remove(args[2]);
                    user.setParticleDeathAvailable(availableEffects);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Removed &f" + args[2] + "&7 from &f" + args[0] + "&7's death-particles."));
                    return true;
                }
                else throw new InvalidArgumentException(sender, "This player does not have this particle: " + args[2]);
            }
            default: return false;
        }
    }
}
