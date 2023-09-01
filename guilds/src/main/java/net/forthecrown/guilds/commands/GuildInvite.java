package net.forthecrown.guilds.commands;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

@Getter
@RequiredArgsConstructor
public class GuildInvite {

  private final UUID guildId;
  private final UUID receiverId;

  public Guild getGuild() {
    return Guilds.getManager().getGuild(guildId);
  }

  public User getReceiver() {
    return Users.get(receiverId);
  }

  // When receiver accepts request
  public void onAccept() {
    Guild guild = getGuild();
    User receiver = getReceiver();

    guild.join(receiver);
    delete();
  }

  // When receiver denies request
  public void onDeny() {
    User receiver = getReceiver();
    Guild guild = getGuild();

    receiver.sendMessage(Messages.REQUEST_DENIED);
    guild.sendMessage(Messages.requestDenied(receiver));

    delete();
  }

  // When sender cancels request by pressing cancel button
  public void onCancel() {
    var guild = getGuild();
    var user = getReceiver();

    guild.announce(Messages.REQUEST_CANCELLED);
    user.sendMessage(Messages.requestCancelled(guild.displayName()));

    delete();
  }

  // Remove request from existence
  public void delete() {
    getGuild().closeInvite(this);
  }

  public boolean equals(GuildInvite other) {
    return guildId.equals(other.guildId) && receiverId.equals(other.receiverId);
  }
}