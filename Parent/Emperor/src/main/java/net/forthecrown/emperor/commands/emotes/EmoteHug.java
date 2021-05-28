package net.forthecrown.emperor.commands.emotes;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatUtils;
import net.forthecrown.emperor.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class EmoteHug extends CommandEmote {

    public EmoteHug() {
        super("hug", 20*3, "&c❤ &7You're too nice of a person &c❤");

        setDescription("Hugs a player");
        register();
    }

    @Override
    protected int execute(CrownUser user, CrownUser recipient) {
        if(Cooldown.contains(recipient, "Emote_Hug_Received")){
            user.sendMessage("&e" + recipient.getName() + " &7has already received some love lol");
            return -1;
        }

        //Do the hugging
        TextComponent hugClick = ChatUtils.convertString(ChatColor.RED + "❤ " + ChatColor.YELLOW + user.getName() + ChatColor.RESET + " hugged you" + ChatColor.YELLOW + " ʕっ•ᴥ•ʔっ" + ChatColor.RED + " ❤")
                .clickEvent(ClickEvent.runCommand("/hug " + user.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Hug them back ❤")));

        recipient.sendMessage(hugClick);
        user.sendMessage("&c❤ &7You hugged &e" + recipient.getName() + " ʕっ•ᴥ•ʔっ &c❤");

        if(recipient.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Cooldown.add(recipient, "Emote_Hug_Received", 10*20);
            new HugTick(recipient);
        }
        return 0;
    }

    @Override
    protected int executeSelf(CrownUser user) {
        user.sendMessage("It's alright to hug yourself &c❤", "We've all got to love ourselves &c❤");
        user.getWorld().spawnParticle(Particle.HEART, user.getLocation().add(0, 0.5, 0), 3, 0.25, 0.25 ,0.25);
        return -1;
    }

    public static class HugTick extends BukkitRunnable {
        private int i = 0;
        private final CrownUser user;

        public HugTick(CrownUser user){
            this.user = user;
            runTaskTimerAsynchronously(CrownCore.inst(), 0, 2);
        }

        @Override
        public void run() {
            if(i == 10*10){
                cancel();
                Cooldown.remove(user, "Emote_Hug_Received");
                return;
            }

            try {
                user.getPlayer().getWorld().spawnParticle(Particle.HEART, user.getPlayer().getLocation().clone().add(0, 1, 0), 1, 0.25, 0.25 ,0.25);
            } catch (NullPointerException ignored) {
                cancel();
                Cooldown.remove(user, "Emote_Hug_Received");
                return;
            }
            i++;
        }
    }
}