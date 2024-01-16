package net.forthecrown.utils.io.source;

import static net.forthecrown.utils.io.source.Sources.CHARSET;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public record PathSource(Path path, Path directory, String name) implements Source {

  @Override
  public StringBuffer read() throws IOException {
    BufferedReader reader = Files.newBufferedReader(path, CHARSET);
    StringWriter writer = new StringWriter();
    reader.transferTo(writer);
    reader.close();

    return writer.getBuffer();
  }

  @Override
  public <S> DataResult<S> save(DynamicOps<S> ops) {
    var builder = ops.mapBuilder();
    String strPath;

    if (directory != null) {
      strPath = directory.relativize(path).toString();
    } else {
      strPath = directory.toString();
    }

    builder.add("path", ops.createString(strPath));

    if (name != null && !Objects.equals(name, strPath)) {
      builder.add("name", ops.createString(name));
    }

    return builder.build(ops.empty());
  }
}