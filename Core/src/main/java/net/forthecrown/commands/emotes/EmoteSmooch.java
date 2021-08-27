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

public class EmoteSmooch extends CommandEmote {

    public EmoteSmooch(){
        super("mwah", 3*20, Component.translatable("emotes.smooch.cooldown"));

        setAliases("smooch", "kiss");
        setDescription("Kisses another player.");

        register();
    }

    static final Component HEART = Component.text("❤").color(NamedTextColor.RED);

    @Override
    public int execute(CrownUser sender, CrownUser target) {
        Location loc = sender.getLocation();

        sender.sendMessage(
                Component.translatable("emotes.smooch.sender", HEART, target.nickDisplayName().color(NamedTextColor.YELLOW))
        );

        target.sendMessage(
                Component.translatable("emotes.smooch.target", HEART, sender.nickDisplayName().color(NamedTextColor.YELLOW))
                        .clickEvent(ClickEvent.runCommand("/" + getName() + " " + sender.getName()))
                        .hoverEvent(HoverEvent.showText(Component.translatable("emotes.smooch.target.hover")))
        );

        if(target.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Location targetLoc = target.getLocation();
            targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            targetLoc.getWorld().spawnParticle(Particle.HEART, targetLoc.getX(), targetLoc.getY()+1, targetLoc.getZ(), 5, 0.5, 0.5, 0.5);

            loc.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
            loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        }
        return 0;
    }

    @Override
    public int executeSelf(CrownUser user) {
        Location loc = user.getPlayer().getLocation();
        user.sendMessage(
                Component.text()
                        .append(Component.translatable("emotes.smooch.self").color(NamedTextColor.YELLOW))
                        .append(Component.text(" ( ^ 3^) ❤"))

                        .hoverEvent(
                                Component.translatable("emotes.smooch.self.hover")
                                        .append(Component.text(" ʕっ•ᴥ•ʔっ"))
                        )
        );
        user.getPlayer().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        user.getPlayer().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        return 0;
    }
}
