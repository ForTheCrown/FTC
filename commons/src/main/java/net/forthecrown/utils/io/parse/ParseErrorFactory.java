package net.forthecrown.utils.io.parse;

import java.io.IOException;

public interface ParseErrorFactory {

  ParseException wrap(Location location, IOException exc);

  ParseException create(Location location, String message, Object... args);
}