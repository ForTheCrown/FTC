package net.forthecrown.core.announcer;

import net.forthecrown.text.ViewerAwareMessage;

interface AnnouncementIterator {

  boolean hasNext();

  void reset();

  ViewerAwareMessage next();
}
