package net.forthecrown.scripts.preprocessor;

import com.mojang.brigadier.context.StringRange;

class SubWriter implements Appendable {

  final StringRange range;
  final StringBuffer buffer;

  int cursor;
  boolean inputErased = false;

  SubWriter(StringRange range, StringBuffer buffer) {
    this.range = range;
    this.buffer = buffer;
  }

  public void deleteInput() {
    int start = range.getStart();
    int end = Math.max(cursor, range.getEnd());

    buffer.delete(start, end);
    cursor = range.getStart();
    inputErased = true;
  }

  @Override
  public SubWriter append(CharSequence csq) {
    boolean insert = inputErased || cursor >= range.getEnd();
    int length = csq.length();
    int newCursor = cursor + length;

    if (insert) {
      buffer.insert(cursor, csq);
    } else {
      buffer.replace(cursor, newCursor, csq.toString());
    }

    cursor = newCursor;
    return this;
  }

  @Override
  public SubWriter append(CharSequence csq, int start, int end) {
    return append(csq.subSequence(start, end));
  }

  @Override
  public SubWriter append(char c) {
    boolean insert = inputErased || cursor >= range.getEnd();

    if (insert) {
      buffer.insert(cursor++, c);
    } else {
      buffer.setCharAt(cursor++, c);
    }

    return this;
  }
}
