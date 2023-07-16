package net.forthecrown.text;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import net.forthecrown.events.ChannelMessageEvent;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

@Getter
public final class ChannelledMessage {

  private final ViewerAwareMessage message;

  private MessageRenderer renderer = MessageRenderer.DEFAULT;
  private Audience source;

  private final Set<Audience> viewers = new ObjectOpenHashSet<>();

  private String channelName = "UNSET";

  private ChannelledMessage(ViewerAwareMessage message) {
    this.message = Objects.requireNonNull(message);
  }

  public static ChannelledMessage create(Component message) {
    return new ChannelledMessage(ViewerAwareMessage.wrap(message));
  }

  public static ChannelledMessage create(ViewerAwareMessage message) {
    return new ChannelledMessage(message);
  }

  public static int announce(Component message) {
    return create(message).setBroadcast().send();
  }

  public static int announce(Audience source, Component message) {
    return create(message).setBroadcast().source(source).send();
  }

  public static int announce(
      Audience source,
      Component message,
      boolean removeSourceFromViewers
  ) {
    return create(message)
        .setBroadcast()
        .source(source, removeSourceFromViewers)
        .send();
  }

  public ChannelledMessage source(Audience source) {
    this.source = Audiences.unwrap(source);
    return this;
  }

  public ChannelledMessage source(Audience source, boolean removeFromViewers) {
    this.source = Audiences.unwrap(source);

    if (removeFromViewers) {
      removeViewer(this.source);
    }

    return this;
  }

  public ChannelledMessage setBroadcast() {
    viewers.add(Bukkit.getConsoleSender());
    viewers.addAll(Bukkit.getOnlinePlayers());
    return this;
  }

  public ChannelledMessage filterViewers(Predicate<Audience> predicate) {
    viewers.removeIf(predicate.negate());
    return this;
  }

  public ChannelledMessage addViewer(Audience viewer) {
    Objects.requireNonNull(viewer);
    viewers.add(Audiences.unwrap(viewer));
    return this;
  }

  public ChannelledMessage removeViewer(Audience viewer) {
    Objects.requireNonNull(viewer);
    viewers.remove(Audiences.unwrap(viewer));
    return this;
  }

  public ChannelledMessage channelName(String name) {
    Objects.requireNonNull(name);
    this.channelName = name;

    return this;
  }

  public ChannelledMessage renderer(MessageRenderer renderer) {
    Objects.requireNonNull(renderer);
    this.renderer = renderer;
    return this;
  }

  /**
   * Announces this announcement object to the all the current {@link #getViewers()}
   * @return {@code -1} if the announcement event was cancelled, {@code 0} if the viewer set was
   *         empty after the event call, or a non-zero number of viewers that saw the announcement
   */
  public int send() {
    ChannelMessageEvent event = new ChannelMessageEvent(
        message,
        source,
        new HashSet<>(viewers),
        channelName
    );

    event.callEvent();

    if (event.isCancelled()) {
      return -1;
    } else if (event.getViewers().isEmpty()) {
      return 0;
    }

    Set<Audience> viewers = event.getViewers();
    final ViewerAwareMessage viewerAwareBase = event.getMessage();

    viewers.forEach(viewer -> {
      Component baseMessage = viewerAwareBase.create(viewer);
      Component msg = renderer.render(viewer, baseMessage);
      viewer.sendMessage(msg);
    });

    return viewers.size();
  }

  public interface MessageRenderer {

    MessageRenderer DEFAULT = (viewer, baseMessage) -> baseMessage;

    MessageRenderer FTC_PREFIX = new MessageRenderer() {
      @Override
      public Component render(Audience viewer, Component baseMessage) {
        return Component.textOfChildren(Messages.FTC_PREFIX, baseMessage);
      }
    };

    Component render(Audience viewer, Component baseMessage);
  }
}