package net.forthecrown.mail;

import java.util.Comparator;

public enum SentDateComparator implements Comparator<Mail> {
  INSTANCE;

  @Override
  public int compare(Mail o1, Mail o2) {
    var firstSent = o1.getSentDate();
    var secondSent = o2.getSentDate();

    assert firstSent != null;
    assert secondSent != null;

    return firstSent.compareTo(secondSent);
  }
}
