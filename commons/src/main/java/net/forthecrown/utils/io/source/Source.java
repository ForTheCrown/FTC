package net.forthecrown.utils.io.source;

import com.google.gson.JsonElement;
import java.io.IOException;
import net.forthecrown.nbt.BinaryTag;

/**
 * A source for a stream of characters
 */
public interface Source {

  /**
   * Reads the source's content
   * @return Source content
   * @throws IOException If an IO Error occurred
   */
  StringBuffer read() throws IOException;

  /**
   * Gets the source's name
   * @return Source's name
   */
  String name();

  /**
   * Saves this source as a JSON element
   * @return JSON representation of this source
   */
  JsonElement save();

  BinaryTag saveAsTag();
}