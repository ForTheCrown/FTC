package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class Hug extends CrownCommand {

    public Hug() {
        super("hug", FtcCore.getInstance());

        setPermission("ftc.emotes.hug");
        setDescription("Hugs a player");
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        if(Cooldown.contains(sender, "Core_Emote_Hug")) throw new InvalidCommandExecution(sender, "&c❤ &7You're too nice of a person &c❤");

        Player player = (Player) sender;
        CrownUser user = FtcCore.getUser(player);

        if(args.length < 1 || args[0].contains(player.getName())){
            user.sendMessage("It's alright to hug yourself &c❤", "We've all got to love ourselves &c❤");
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 3, 0.25, 0.25 ,0.25);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) throw new InvalidPlayerInArgument(sender, args[0]);
        CrownUser targetUser = FtcCore.getUser(target);

        if(!targetUser.allowsEmotes()) throw new EmoteDisabledException(sender).targetDisabled();
        if(!user.allowsEmotes()) throw new EmoteDisabledException(sender).senderDisabled();

        if(Cooldown.contains(targetUser, "Emote_Hug_Received")){
            user.sendMessage("&e" + targetUser.getName() + " &7has already received some love lol");
            return true;
        }
        Cooldown.add(targetUser, "Emote_Hug_Received", null);
        Cooldown.add(sender, "Core_Emote_Hug", 20*3);

        //Do the hugging
        TextComponent toSend = new TextComponent(ChatColor.RED + "❤ " + ChatColor.YELLOW + user.getName() + ChatColor.RESET + " hugged you" + ChatColor.YELLOW + " ʕっ•ᴥ•ʔっ" + ChatColor.RED + " ❤");
        toSend.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hug " + user.getName()));
        toSend.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Hug them back ❤")));

        target.sendMessage(toSend);
        user.sendMessage("&c❤ &7You hugged &e" + target.getName() + " ʕっ•ᴥ•ʔっ &c❤");

        if(target.getGameMode() != GameMode.SPECTATOR) new HugTick(targetUser);
        else Cooldown.remove(target, "Emote_Hug_Received");

        return true;
    }
}
