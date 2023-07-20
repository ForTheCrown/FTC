package net.forthecrown.text;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.events.ChannelMessageEvent;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

@Getter
public final class ChannelledMessage {

  private final ViewerAwareMessage message;

  private MessageRenderer renderer = MessageRenderer.DEFAULT;
  private MessageHandler handler = MessageHandler.DEFAULT;

  private Audience source;

  private final Set<Audience> targets = new ObjectOpenHashSet<>();

  private boolean shownToSender = true;

  private String channelName = "UNSET";

  @Setter @Getter
  @Accessors(chain = true)
  private boolean announcement;

  private ChannelledMessage(ViewerAwareMessage message) {
    this.message = Objects.requireNonNull(message);
  }

  public static ChannelledMessage create(Component message) {
    return new ChannelledMessage(ViewerAwareMessage.wrap(message));
  }

  public static ChannelledMessage create(ViewerAwareMessage message) {
    return new ChannelledMessage(message);
  }

  public static int announce(ViewerAwareMessage message) {
    return create(message).setBroadcast().send();
  }

  public static int announce(Component message) {
    return create(message).setBroadcast().send();
  }

  public static int announce(Audience source, Component message) {
    return create(message).setBroadcast().setSource(source).send();
  }

  public static int announce(
      Audience source,
      Component message,
      boolean removeSourceFromViewers
  ) {
    return create(message)
        .setBroadcast()
        .setSource(source, removeSourceFromViewers)
        .send();
  }

  public ChannelledMessage shownToSource(boolean shown) {
    this.shownToSender = shown;
    return this;
  }

  public ChannelledMessage setSource(Audience source) {
    this.source = Audiences.unwrap(source);
    return this;
  }

  public ChannelledMessage setSource(Audience source, boolean removeFromViewers) {
    this.source = Audiences.unwrap(source);

    if (removeFromViewers) {
      removeTarget(this.source);
      shownToSender = false;
    }

    return this;
  }

  public ChannelledMessage setBroadcast() {
    targets.add(Bukkit.getConsoleSender());
    targets.addAll(Bukkit.getOnlinePlayers());
    setAnnouncement(true);
    return this;
  }

  public ChannelledMessage filterTargets(Predicate<Audience> predicate) {
    targets.removeIf(predicate.negate());
    return this;
  }

  public ChannelledMessage addTarget(Audience viewer) {
    Objects.requireNonNull(viewer);
    targets.add(Audiences.unwrap(viewer));
    return this;
  }

  public ChannelledMessage removeTarget(Audience viewer) {
    Objects.requireNonNull(viewer);
    targets.remove(Audiences.unwrap(viewer));
    return this;
  }

  public ChannelledMessage setChannelName(String name) {
    Objects.requireNonNull(name);
    this.channelName = name;

    return this;
  }

  public ChannelledMessage setRenderer(MessageRenderer renderer) {
    Objects.requireNonNull(renderer);
    this.renderer = renderer;
    return this;
  }

  public ChannelledMessage setHandler(MessageHandler handler) {
    Objects.requireNonNull(handler);
    this.handler = handler;
    return this;
  }

  /**
   * Sends this message object to the all the 'viewers'
   * <p>
   * Under most circumstances, 'viewers' means all {@link #getTargets()} and {@link #getSource()},
   * however if {@link #isShownToSender()} is set to {@code false}, then the source of the message
   * will not be included in the message
   *
   * @return {@code -1} if the announcement event was cancelled, {@code 0} if the viewer set was
   *         empty after the event call, or a non-zero number of viewers that saw the announcement
   */
  public int send() {
    if (source != null) {
      targets.removeIf(audience -> Audiences.equals(audience, source));
    }

    ChannelMessageEvent event = new ChannelMessageEvent(this);
    event.callEvent();

    if (event.isCancelled()) {
      return -1;
    }

    return handler.handle(this, event);
  }

  public static interface MessageRenderer {

    MessageRenderer DEFAULT = (viewer, baseMessage) -> baseMessage;

    MessageRenderer FTC_PREFIX = new MessageRenderer() {
      @Override
      public Component render(Audience viewer, Component baseMessage) {
        return Component.textOfChildren(Messages.FTC_PREFIX, baseMessage);
      }
    };

    Component render(Audience viewer, Component baseMessage);
  }

  public static interface MessageHandler {

    MessageHandler DEFAULT = (channelled, event) -> {
      MessageRenderer renderer = channelled.getRenderer();
      Audience source = channelled.getSource();
      Set<Audience> viewers = event.getTargets();
      ViewerAwareMessage message = event.getMessage();
      boolean shownToSender = event.isShownToSender();

      int seenMessage = viewers.size();
      viewers.forEach(viewer -> sendMessage(viewer, message, renderer));

      if (shownToSender && source != null) {
        sendMessage(source, message, renderer);
        seenMessage++;
      }

      return seenMessage;
    };

    MessageHandler EMPTY_IF_NOT_VIEWING = (ch, event) -> {
      Set<Audience> viewers = new HashSet<>(ch.getTargets());

      if (event.isShownToSender() && event.getSource() != null) {
        viewers.add(event.getSource());
      }

      viewers.forEach(audience -> {
        boolean showMessage
            = event.getTargets().contains(audience)
            || Audiences.equals(ch.getSource(), audience);

        Component baseMessage = !showMessage
            ? Component.empty()
            : event.getMessage().create(audience);

        Component message = ch.getRenderer().render(audience, baseMessage);
        audience.sendMessage(message);
      });

      return viewers.size();
    };

    int handle(
        ChannelledMessage channelled,
        ChannelMessageEvent event
    );

    static void sendMessage(
        Audience viewer,
        ViewerAwareMessage message,
        MessageRenderer renderer
    ) {
      Component baseMessage = message.create(viewer);
      Component msg = renderer.render(viewer, baseMessage);
      viewer.sendMessage(msg);
    }
  }
}