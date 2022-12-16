package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class StreakIncreaseEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final User user;
    private final StreakCategory category;
    private final int streak;
    private final ChallengeEntry entry;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}