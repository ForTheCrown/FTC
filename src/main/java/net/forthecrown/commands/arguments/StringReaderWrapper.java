package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import java.io.IOException;
import java.io.Reader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor(staticName = "wrap")
class StringReaderWrapper extends Reader {
  private int mark;
  private final StringReader reader;

  @Override
  public int read(@NotNull char[] cbuf, int off, int len) {
    if (!reader.canRead()) {
      return -1;
    }

    int read = 0;

    for (int i = 0; i < len; i++) {
      if (!reader.canRead()) {
        break;
      }

      cbuf[off + i] = reader.read();
      read++;
    }

    return read;
  }

  @Override
  public int read() {
    if (!reader.canRead()) {
      return -1;
    }
    return reader.read();
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public void mark(int readAheadLimit) throws IOException {
    this.mark = reader.getCursor();
  }

  @Override
  public void reset() {
    reader.setCursor(mark);
  }

  @Override
  public void close() {

  }
}