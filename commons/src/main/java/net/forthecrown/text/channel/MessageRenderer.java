package net.forthecrown.text.channel;

import net.forthecrown.text.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface MessageRenderer {

  MessageRenderer DEFAULT = (viewer, baseMessage) -> baseMessage;

  MessageRenderer FTC_PREFIX = prefixing(Messages.FTC_PREFIX);

  static MessageRenderer prefixing(Component prefix) {
    return (viewer, baseMessage) -> Component.textOfChildren(prefix, baseMessage);
  }

  Component render(Audience viewer, Component baseMessage);

  default MessageRenderer then(MessageRenderer renderer) {
    return (viewer, baseMessage) -> {
      var rendered = MessageRenderer.this.render(viewer, baseMessage);
      return renderer.render(viewer, rendered);
    };
  }
}
