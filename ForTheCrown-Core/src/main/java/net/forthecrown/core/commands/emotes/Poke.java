package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.EmoteDisabledException;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class Poke extends CrownCommand {

    public Poke(){
        super("poke", FtcCore.getInstance());

        setPermission("ftc.emotes");
        setDescription("Pokes another player.");
        register();
    }

    private final static List<String> pokeOwies = Arrays.asList("stomach", "back", "arm", "butt", "cheek", "neck");

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        Player player = (Player) sender;
        CrownUser playerData = FtcCore.getUser(player.getUniqueId());

        // Sender can't be on cooldown:
        if(Cooldown.contains(player, "Core_Emote_Poke")) throw new InvalidCommandExecution(player, "You poke people too often lol");

        // Command no args or target = sender:
        if (args.length < 1 || args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage("You poked yourself! Weirdo"); //Damn, some people really be weird, pokin themselves, couldn't be me ( ._.)
            player.getWorld().playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
            return true;
        }

        // Sender should have emotes enabled:
        if(!playerData.allowsEmotes()) throw new EmoteDisabledException(sender).senderDisabled();

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null){
            player.sendMessage(args[0] + " is not a currently online player.");
            return false;
        }
        CrownUser targetData = FtcCore.getUser(target.getUniqueId());

        if(!targetData.allowsEmotes()) throw new EmoteDisabledException(sender).targetDisabled();

        // Actual poking:
        int pokeOwieInt = (int)(Math.random()*pokeOwies.size()); //The random int that determines what body part they'll poke lol
        player.sendMessage("You poked " + ChatColor.YELLOW + target.getName() + "'s " + ChatColor.RESET + pokeOwies.get(pokeOwieInt));

        if(target.getGameMode() != GameMode.SPECTATOR){
            target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RESET + " poked your " + pokeOwies.get(pokeOwieInt));
            target.getWorld().playSound(target.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
            target.setVelocity(target.getVelocity().add(target.getLocation().getDirection().normalize().multiply(-0.3).setY(.1)));
        }

        // Put sender on cooldown:
        Cooldown.add(player, "Core_Emote_Poke", 5*20);
        return true;
    }
}
