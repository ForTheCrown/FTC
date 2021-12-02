package net.forthecrown.commands.emotes;

import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.CrownRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EmotePoke extends CommandEmote {

    public EmotePoke(){
        super("poke", 5*20);

        setDescription("Pokes a player.");
        register();
    }

    private static final String TRANS_PREFIX = "emotes.poke.part.";
    private final static Component[] PARTS = {
            Component.translatable(TRANS_PREFIX + "stomach"),
            Component.translatable(TRANS_PREFIX + "back"),
            Component.translatable(TRANS_PREFIX + "arm"),
            Component.translatable(TRANS_PREFIX + "butt"),
            Component.translatable(TRANS_PREFIX + "cheek"),
            Component.translatable(TRANS_PREFIX + "neck"),
            Component.translatable(TRANS_PREFIX + "belly")
    };

    private static final CrownRandom RANDOM = new CrownRandom();

    private static Component randomPart() {
        return PARTS[RANDOM.intInRange(0, PARTS.length-1)];
    }

    @Override
    public int execute(CrownUser sender, CrownUser recipient) {
        Component part = randomPart();

        sender.sendMessage(
                Component.translatable("emotes.poke.sender",
                        recipient.nickDisplayName().color(NamedTextColor.YELLOW),
                        part
                )
        );

        recipient.sendMessage(
                Component.translatable("emotes.poke.target",
                        sender.nickDisplayName().color(NamedTextColor.YELLOW),
                        part
                )
        );

        if(recipient.getPlayer().getGameMode() != GameMode.SPECTATOR){
            Player target = recipient.getPlayer();
            Location targetLoc = recipient.getLocation();

            targetLoc.getWorld().playSound(target.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
            target.setVelocity(target.getVelocity().add(target.getLocation().getDirection().normalize().multiply(-0.3).setY(.1)));
        }
        return 0;
    }

    @Override
    public int executeSelf(CrownUser user) {
        user.sendMessage(Component.translatable("emotes.poke.self"));

        user.getWorld().playSound(user.getLocation(), Sound.ENCHANT_THORNS_HIT, 3.0F, 1.8F);
        return 0;
    }
}
