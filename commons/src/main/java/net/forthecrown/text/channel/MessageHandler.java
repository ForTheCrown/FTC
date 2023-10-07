package net.forthecrown.text.channel;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.forthecrown.events.ChannelMessageEvent;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public interface MessageHandler {

  MessageHandler DEFAULT = (channelled, event) -> {
    MessageRenderer renderer = event.getRenderer();
    ViewerAwareMessage message = event.getMessage();

    Audience source = channelled.getSource();
    Set<Audience> viewers = event.getTargets();

    int seenMessage = viewers.size();

    if (event.getState() != ChannelMessageState.SOFT_CANCELLED) {
      viewers.forEach(viewer -> sendMessage(viewer, message, renderer));
    }

    if (event.isShownToSender() && source != null) {
      sendMessage(source, message, renderer);
      seenMessage++;
    }

    return seenMessage;
  };

  MessageHandler EMPTY_IF_VIEWER_WAS_REMOVED = (ch, event) -> {
    Set<Audience> viewers = new HashSet<>();

    if (event.getState() != ChannelMessageState.SOFT_CANCELLED) {
      viewers.addAll(ch.getTargets());
    }

    if (event.isShownToSender() && event.getSource() != null) {
      viewers.add(event.getSource());
    }

    viewers.forEach(audience -> {
      boolean showMessage
          = event.getTargets().contains(audience)
          || Audiences.equals(ch.getSource(), audience);

      Component baseMessage = showMessage
          ? event.getMessage().create(audience)
          : Component.empty();

      Component message = event.getRenderer().render(audience, baseMessage);
      audience.sendMessage(message);
    });

    return viewers.size();
  };

  int handle(ChannelledMessage channelled, ChannelMessageEvent event);

  default MessageHandler postHandle(Consumer<ChannelMessageEvent> consumer) {
    return (channelled, event) -> {
      int result = MessageHandler.this.handle(channelled, event);
      consumer.accept(event);
      return result;
    };
  }

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
