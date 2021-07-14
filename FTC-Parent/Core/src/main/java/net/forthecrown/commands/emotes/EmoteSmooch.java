package net.forthecrown.commands.emotes;

import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
    public int execute(CrownUser sender, CrownUser recipient) {
        Location loc = sender.getLocation();

        sender.sendMessage(
                Component.text()
                        .append(Component.text("❤").color(NamedTextColor.RED))
                        .append(Component.text(" You smooched "))
                        .append(recipient.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(Component.text(" "))
                        .append(Component.text("❤").color(NamedTextColor.RED))
                        .build()
        );

        TextComponent text = Component.text()
                .append(Component.text("❤").color(NamedTextColor.RED))
                .append(Component.space())
                .append(sender.nickDisplayName().color(NamedTextColor.YELLOW))
                .append(Component.text(" smooched you!"))
                .append(Component.space())
                .append(Component.text("❤").color(NamedTextColor.RED))

                .clickEvent(ClickEvent.runCommand("/mwah " + sender.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Click to smooch them back")))

                .build();

        recipient.sendMessage(text);

        if(recipient.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Location targetLoc = recipient.getLocation();
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
                        .append(Component.text("Love yourself!").color(NamedTextColor.YELLOW))
                        .append(Component.text(" ( ^ 3^) ❤"))

                        .hoverEvent(Component.text("You're amazing! ʕっ•ᴥ•ʔっ"))
        );
        user.getPlayer().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        user.getPlayer().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        return 0;
    }
}
