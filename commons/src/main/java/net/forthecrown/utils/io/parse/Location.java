package net.forthecrown.utils.io.parse;

public interface Location {

  Location ZERO = new LocationImpl(0, 0, 0);

  static Location of(int line, int col, int index) {
    if (line == 0 && col == 0 && index == 0) {
      return ZERO;
    }

    return new LocationImpl(line, col, index);
  }

  int line();

  int column();

  int index();
}