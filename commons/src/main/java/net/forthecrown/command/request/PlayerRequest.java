package net.forthecrown.command.request;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.audience.Audience;
import org.bukkit.scheduler.BukkitTask;

public abstract class PlayerRequest {

  @Getter
  private final UUID senderId;

  @Getter
  private final UUID targetId;

  BukkitTask expiryTask;
  RequestTable table;

  public PlayerRequest(UUID senderId, UUID targetId) {
    Objects.requireNonNull(senderId);
    Objects.requireNonNull(targetId);

    this.senderId = senderId;
    this.targetId = targetId;
  }

  protected void validate(Audience viewer) throws CommandSyntaxException {
    if (table == null) {
      return;
    }

    table.validate(this, viewer);
  }

  public void start() {
    if (Tasks.isScheduled(expiryTask)) {
      return;
    }

    Duration dur = getExpiryDuration();
    expiryTask = Tasks.runLater(this::cancel, dur);
  }

  public void stop() {
    Tasks.cancel(expiryTask);
    expiryTask = null;

    if (table != null) {
      table.remove(this);
    }
  }

  public User getTarget() {
    return Users.get(targetId);
  }

  public User getSender() {
    return Users.get(senderId);
  }

  protected abstract Duration getExpiryDuration();

  public void onBegin() {

  }

  public void cancel() {

  }

  public void accept() throws CommandSyntaxException {
    try {
      var target = getTarget();
      validate(target);
    } catch (CommandSyntaxException exc) {
      table.remove(this);
      throw exc;
    }


  }

  public void deny() {

  }
}
