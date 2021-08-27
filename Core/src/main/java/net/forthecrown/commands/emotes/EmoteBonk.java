package net.forthecrown.commands.emotes;

import net.forthecrown.user.CrownUser;
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
        super("bonk", 3*20);

        setDescription("Bonks a player");
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
    public int execute(CrownUser sender, CrownUser target) {
        Location loc = target.getPlayer().getLocation();
        loc.setPitch(loc.getPitch() + 20F);

        sender.sendMessage(
                Component.translatable("emotes.bonk.sender", target.nickDisplayName().color(NamedTextColor.YELLOW))
        );

        target.sendMessage(
                Component.translatable("emotes.bonk.target", sender.nickDisplayName().color(NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.runCommand("/bonk " + sender.getName()))
                        .hoverEvent(HoverEvent.showText(Component.translatable("emotes.bonk.target.hover")))
        );

        if(target.getPlayer().getGameMode() != GameMode.SPECTATOR){
            target.getPlayer().teleport(loc);
            target.getPlayer().getWorld().playSound(loc, Sound.ENTITY_SHULKER_HURT_CLOSED, 2.0F, 0.8F);
            target.getPlayer().getWorld().spawnParticle(Particle.CRIT, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        }
        return 0;
    }

    @Override
    public int executeSelf(CrownUser user) {
        user.sendMessage(
                Component.translatable("emotes.bonk.self", EmoteSmooch.HEART)
        );
        return 0;
    }
}
