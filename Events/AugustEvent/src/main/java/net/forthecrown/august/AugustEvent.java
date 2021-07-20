package net.forthecrown.august;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.crownevents.CrownEvent;
import net.forthecrown.crownevents.CrownEventUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

public class AugustEvent implements CrownEvent<AugustEntry> {
    public static AugustEntry currentEntry;

    @Override
    public void start(Player player) throws CommandSyntaxException {
        checkCanEnter();

    }

    @Override
    public void end(AugustEntry entry) {
        if(!entry.timer().wasStopped()) entry.timer().stop();

        entry.player().teleport(EventConstants.EXIT);
        entry.inEventListener().unregister();

        currentEntry = null;
    }

    @Override
    public void complete(AugustEntry entry) {
        Score record = EventConstants.CROWN.getScore(entry.player().getName());

        TextComponent.Builder builder = Component.text();

        if(CrownEventUtils.isNewRecord(record, entry.score())) {
            builder.append(Component.text("New record!").color(NamedTextColor.GOLD));
            record.setScore(entry.score());
        } else builder.append(Component.text("Better luck next time!").color(NamedTextColor.GRAY));

        entry.player().sendMessage(
                builder.append(Component.text(" Score: ")
                        .append(Component.text(entry.score()))
                ).build()
        );

        end(entry);
    }

    public void checkCanEnter() throws CommandSyntaxException {
        if(AugustEvent.currentEntry != null) throw FtcExceptionProvider.create("There is already someone in the event");
    }
}
