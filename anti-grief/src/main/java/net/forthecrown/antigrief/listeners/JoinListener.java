package net.forthecrown.antigrief.listeners;

import net.forthecrown.antigrief.GriefPermissions;
import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.antigrief.StaffNote;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.event.UserJoinEvent;
import net.forthecrown.utils.Audiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class JoinListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onUserJoin(UserJoinEvent event) {
    User user = event.getUser();

    PunishEntry entry = Punishments.entry(user);
    assert entry != null;

    var notes = entry.getNotes();

    if (notes == null || notes.isEmpty()) {
      return;
    }

    var writer = TextWriters.newWriter();
    StaffNote.writeNotes(notes, writer, user);

    ChannelledMessage ch = ChannelledMessage.create(writer.asComponent());
    ch.setBroadcast();
    ch.filterTargets(audience -> {
      User viewer = Audiences.getUser(audience);

      if (viewer == null) {
        return false;
      }

      if (!viewer.hasPermission(GriefPermissions.PUNISH_NOTES)) {
        return false;
      }

      return viewer.get(StaffNote.VIEWS_NOTES);
    });
    ch.send();
  }
}
