package net.forthecrown.august.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.august.AugustPlugin;
import net.forthecrown.august.EventConstants;
import net.forthecrown.august.EventUtil;
import net.forthecrown.crownevents.CrownEvent;
import net.forthecrown.crownevents.CrownEventUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

public class PinataEvent implements CrownEvent<AugustEntry> {
    public static AugustEntry currentEntry;
    public static EventStarter currentStarter;

    @Override
    public void start(Player player) throws CommandSyntaxException {
        if(!EventUtil.canEnter(player)) return;

        AugustPlugin.reporter.logEntry(player);
        currentStarter = new EventStarter(player);
    }

    @Override
    public void end(AugustEntry entry) {
        if(!entry.timer().wasStopped()) entry.timer().stop();

        entry.player().teleport(EventConstants.EXIT);
        entry.inEventListener().unregister();

        EventUtil.killAllBebes();

        currentEntry = null;
        AugustPlugin.reporter.logExit(entry.player());
    }

    @Override
    public void complete(AugustEntry entry) {
        Score record = CrownEventUtils.crownObjective().getScore(entry.player().getName());
        AugustPlugin.reporter.logExit(entry.player(), entry.score(), "Record: " + record.getScore());

        TextComponent.Builder builder = Component.text();

        if(CrownEventUtils.isNewRecord(record, entry.score())) {
            builder.append(Component.text("You got a new record!").color(NamedTextColor.GOLD));
            record.setScore(entry.score());
        } else builder.append(Component.text("Better luck next time! :(").color(NamedTextColor.AQUA));

        entry.player().sendMessage(
                builder.append(Component.text(" Score: ")
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(entry.score()))
                ).build()
        );

        end(entry);
    }
}
