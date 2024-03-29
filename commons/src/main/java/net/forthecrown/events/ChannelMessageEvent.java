package net.forthecrown.events;

import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelMessageState;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageRenderer;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event called when a message is sent to a viewer or viewers via a
 * {@link ChannelledMessage}
 */
@Getter
public class ChannelMessageEvent extends Event implements Cancellable {

  public static final String UNSET_NAME = "UNSET";
  @Getter
  private static final HandlerList handlerList = new HandlerList();

  /**
   * The current message
   */
  private ViewerAwareMessage message;

  /**
   * The original, unedited message
   */
  private final ViewerAwareMessage initialMessage;

  /**
   * Source of the broadcast, may be null.
   */
  private final Audience source;

  /**
   * A set of audiences that will view the message.
   * <p>
   * Note: This set does NOT include the source
   */
  private final Set<Audience> targets;

  /**
   * The name of the channel the message is being sent in.
   * <p>
   * This name is arbitrary. By default, the value of this name will be "UNSET"
   */
  private final String channelName;

  /**
   * Determines if the message is also shown to the 'source' of the message
   * @see #getSource()
   */
  private final boolean shownToSender;

  private final boolean announcement;

  private final MessageRenderer renderer;

  @Getter @Setter
  private ChannelMessageState state;

  public ChannelMessageEvent(ChannelledMessage message) {
    super(!Bukkit.isPrimaryThread());

    this.initialMessage = message.getMessage();
    this.message        = message.getMessage();
    this.source         = message.getSource();
    this.targets        = message.getTargets();
    this.channelName    = message.getChannelName();
    this.shownToSender  = message.isShownToSender();
    this.announcement   = message.isAnnouncement();
    this.renderer       = message.getRenderer();

    this.state = ChannelMessageState.FINE;
  }

  @Override
  public boolean isCancelled() {
    return state != ChannelMessageState.FINE;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.state = cancel ? ChannelMessageState.CANCELLED : ChannelMessageState.FINE;
  }

  /**
   * Sets the message to be broadcast
   * @param message Message
   */
  public void setMessage(@NotNull ViewerAwareMessage message) {
    Objects.requireNonNull(message);
    this.message = message;
  }

  /**
   * Gets the {@link #getSource()} as a {@link User}
   * @return User source, or {@code null}, if the source is null or if the source wasn't a
   *         user or player
   */
  public @Nullable User getUserSource() {
    return Audiences.getUser(source);
  }

  /**
   * Gets the {@link #getSource()} as a {@link Player} object
   * @return Player source, or {@code null}, if the source is null or if the source couldn't be
   *         converted to a player
   */
  public @Nullable Player getPlayerSource() {
    return Audiences.getPlayer(source);
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}