package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class EmoteSmooch extends CommandEmote {

    public EmoteSmooch(){
        super("mwah", 3*20, "You kiss too much lol");

        setAliases("smooch", "kiss");
        setPermission("ftc.emotes");
        setDescription("Kisses another player.");
        register();
    }

    @Override
    protected int execute(CrownUser sender, CrownUser recipient) {
        Location loc = recipient.getLocation();

        sender.sendMessage(ChatColor.RED + "❤" + ChatColor.RESET + " You smooched " + recipient.getName() + ChatColor.RED + " ❤");

        TextComponent text = ComponentUtils.convertString("&c❤ &e" + sender.getName() + " &rsmooched you! &r( ^ 3^) &c❤")
                .clickEvent(ClickEvent.runCommand("/mwah " + sender.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Click to smooch them back")));

        recipient.sendMessage(text);

        if(recipient.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Location targetLoc = recipient.getLocation();
            targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            targetLoc.getWorld().spawnParticle(Particle.HEART, targetLoc.getX(), targetLoc.getY()+1, targetLoc.getZ(), 5, 0.5, 0.5, 0.5);
        }

        loc.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);

        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        Location loc = user.getPlayer().getLocation();
        user.getPlayer().sendMessage(ChatColor.YELLOW + "Love yourself!" + ChatColor.RESET + " ( ^ 3^) ❤");
        user.getPlayer().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        user.getPlayer().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        return 0;
    }
}
