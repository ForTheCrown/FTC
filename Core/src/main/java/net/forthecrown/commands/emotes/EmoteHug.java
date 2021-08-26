package net.forthecrown.commands.emotes;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class EmoteHug extends CommandEmote {

    public EmoteHug() {
        super("hug", 20*3, Component.translatable("emotes.hug.cooldown", EmoteSmooch.HEART));

        setDescription("Hugs a player");
        setPermission(Permissions.EMOTE_HUG);

        register();
    }

    @Override
    public int execute(CrownUser user, CrownUser target) {
        if(Cooldown.contains(target, "Emote_Hug_Received")){
            user.sendMessage(
                    Component.translatable("emotes.hug.sender.cooldown", target.nickDisplayName().color(NamedTextColor.YELLOW))
            );

            return -1;
        }

        target.sendMessage(Component.translatable("emotes.hug.target", EmoteSmooch.HEART, user.nickDisplayName().color(NamedTextColor.YELLOW), Component.text("ʕっ•ᴥ•ʔっ"))
                .clickEvent(ClickEvent.runCommand("/" + getName() + " " + user.getName()))
                .hoverEvent(Component.text("Hug them back ❤"))
        );

        user.sendMessage(
                Component.translatable("emotes.hug.sender", EmoteSmooch.HEART, target.nickDisplayName().color(NamedTextColor.YELLOW))
        );

        if(target.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Cooldown.add(target, "Emote_Hug_Received", 10*20);
            new HugTick(target);
        }
        return 0;
    }

    @Override
    public int executeSelf(CrownUser user) {
        user.sendMessage(
                Component.translatable("emotes.hug.self", EmoteSmooch.HEART)
        );

        user.getWorld().spawnParticle(Particle.HEART, user.getLocation().add(0, 0.5, 0), 3, 0.25, 0.25 ,0.25);
        return -1;
    }

    public static class HugTick extends BukkitRunnable {
        private int i = 0;
        private final CrownUser user;

        public HugTick(CrownUser user){
            this.user = user;
            runTaskTimerAsynchronously(Crown.inst(), 0, 2);
        }

        @Override
        public void run() {
            if(i == 100){
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