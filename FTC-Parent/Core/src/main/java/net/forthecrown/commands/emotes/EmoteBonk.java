package net.forthecrown.commands.emotes;

import net.forthecrown.core.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
     * Bonks a player
     *
     * Valid usages of command:
     * - /bonk <player>
     *
     * Main Author: Botul
     */

    @Override
    protected int execute(CrownUser sender, CrownUser recipient) {
        Location loc = recipient.getPlayer().getLocation();
        loc.setPitch(loc.getPitch() + 20F);

        sender.sendMessage(
                Component.text("You bonked ")
                        .append(recipient.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(Component.text("!"))
        );

        recipient.sendMessage(Component.text()
                .append(sender.nickDisplayName().color(NamedTextColor.YELLOW))
                .append(Component.text(" bonked you!"))
                .clickEvent(ClickEvent.runCommand("/bonk " + sender.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Bonk them back! Go on, do it!")))
                .build()
        );

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
