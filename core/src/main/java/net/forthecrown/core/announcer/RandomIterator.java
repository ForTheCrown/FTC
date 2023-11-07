package net.forthecrown.core.announcer;

import java.util.List;
import java.util.Random;
import net.forthecrown.text.ViewerAwareMessage;

class RandomIterator implements AnnouncementIterator {

  static final Random random = new Random();

  private final List<ViewerAwareMessage> messages;

  public RandomIterator(List<ViewerAwareMessage> messages) {
    this.messages = messages;
  }

  @Override
  public boolean hasNext() {
    return !messages.isEmpty();
  }

  @Override
  public void reset() {

  }

  @Override
  public ViewerAwareMessage next() {
    int index = random.nextInt(messages.size());
    return messages.get(index);
  }
}
