package net.forthecrown.guilds.listeners;

import java.time.DayOfWeek;
import net.forthecrown.events.DayChangeEvent;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildMessages;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class DayChangeListener implements Listener {

  private final GuildManager manager;

  public DayChangeListener(GuildManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true)
  public void onDayChange(DayChangeEvent event) {
    manager.resetDailyExpEarnedAmounts();
    var time = event.getTime();

    // If start of weekend or end of weekend, announce multiplier state change
    if (time.getDayOfWeek() == DayOfWeek.SATURDAY) {
      ChannelledMessage.announce(viewer -> {
        User user = Audiences.getUser(viewer);
        if (user == null) {
          return null;
        }

        float mod = manager.getExpModifier().getModifier(user.getUniqueId());
        return GuildMessages.weekendMultiplierActive(mod);
      });
    } else if (time.getDayOfWeek() == DayOfWeek.MONDAY) {
      ChannelledMessage.announce(GuildMessages.WEEKEND_MULTIPLIER_INACTIVE);
    }
  }
}
