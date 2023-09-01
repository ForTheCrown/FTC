package net.forthecrown.core.announcer;

import java.util.Collections;
import java.util.List;
import net.forthecrown.text.ViewerAwareMessage;

enum Order {
  INCREMENTING {
    @Override
    AnnouncementIterator createIterator(List<ViewerAwareMessage> messages) {
      return new IncIterator(1, messages);
    }
  },

  DECREMENTING {
    @Override
    AnnouncementIterator createIterator(List<ViewerAwareMessage> messages) {
      return new IncIterator(-1, messages);
    }
  },

  RANDOM {
    @Override
    AnnouncementIterator createIterator(List<ViewerAwareMessage> messages) {
      return new RandomIterator(messages);
    }
  },

  SHUFFLE {
    @Override
    AnnouncementIterator createIterator(List<ViewerAwareMessage> messages) {
      Collections.shuffle(messages);
      return new IncIterator(1, messages);
    }
  };

  abstract AnnouncementIterator createIterator(List<ViewerAwareMessage> messages);
}
