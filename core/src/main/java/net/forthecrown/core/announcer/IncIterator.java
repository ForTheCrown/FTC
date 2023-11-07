package net.forthecrown.core.announcer;

import java.util.List;
import net.forthecrown.text.ViewerAwareMessage;

class IncIterator implements AnnouncementIterator {

  final int direction;
  final List<ViewerAwareMessage> messages;

  int nextIndex = 0;

  public IncIterator(int direction, List<ViewerAwareMessage> messages) {
    this.direction = direction;
    this.messages = messages;
  }

  @Override
  public boolean hasNext() {
    return nextIndex >= 0 && nextIndex < messages.size();
  }

  @Override
  public void reset() {
    if (direction < 0) {
      nextIndex = messages.size() - 1;
    } else {
      nextIndex = 0;
    }
  }

  @Override
  public ViewerAwareMessage next() {
    int index = nextIndex;
    nextIndex += direction;

    return messages.get(index);
  }
}
