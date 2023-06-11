package net.forthecrown.utils.io.source;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import net.forthecrown.utils.io.JsonWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Sources {
  private Sources() {}

  public static final Charset CHARSET = StandardCharsets.UTF_8;

  public static Source fromPath(@NotNull Path path) {
    return fromPath(path, null);
  }

  public static Source fromPath(@NotNull Path path, @Nullable Path scriptDirectory) {
    Objects.requireNonNull(path, "Null path");

    String name;

    if (scriptDirectory == null) {
      name = path.toString();
    } else {
      Path relative = scriptDirectory.relativize(path);
      name = relative.toString();
    }

    return new PathSource(path, name);
  }

  public static Source fromUrl(@NotNull String url) throws MalformedURLException {
    Objects.requireNonNull(url, "Null url");
    URL urlObject = new URL(url);
    return fromUrl(urlObject);
  }

  public static Source fromUrl(@NotNull URL url, String name) {
    Objects.requireNonNull(url, "Null url");
    name = Objects.requireNonNullElse(name, url.toString());
    return new UrlSource(url, name);
  }

  public static Source fromUrl(@NotNull URL url) {
    return fromUrl(url, null);
  }

  public static Source direct(@NotNull CharSequence src) {
    return direct(src, null);
  }

  public static Source direct(@NotNull CharSequence src, @Nullable String name) {
    Objects.requireNonNull(src, "Null source");

    name = Objects.requireNonNullElse(name, "<eval>");
    return new DirectSource(src, name);
  }

  public static Source loadFromJson(
      JsonElement element,
      Path parentDirectory,
      boolean assumeDirect
  ) {
    Objects.requireNonNull(element, "Null source element");

    if (element.isJsonPrimitive()) {
      var str = element.getAsString();
      return assumeDirect ? direct(str) : fromPath(parentDirectory.resolve(str), parentDirectory);
    }

    Preconditions.checkArgument(element.isJsonObject(),
        "Element must either be primitive or object"
    );

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    String name = json.getString("name");

    if (json.has("path")) {
      String path = json.getString("path");
      return fromPath(parentDirectory.resolve(path), parentDirectory);
    }

    if (json.has("url")) {
      String urlString = json.getString("url");

      try {
        URL url = new URL(urlString);
        return fromUrl(url, name);
      } catch (IOException exc) {
        throw new IllegalStateException(exc);
      }
    }

    if (json.has("raw")) {
      String code = json.getString("raw");
      return direct(code, name);
    }

    // Backwards compatability with 'js' value in JSON
    if (json.has("js")) {
      String code = json.getString("js");
      return direct(code, name);
    }

    throw new IllegalArgumentException(
        "JSON object must have 1 of 3 values declared, a 'url', 'path', or 'raw' value"
            + ", optional 'name' value is also allowed"
            + ", json=" + element
    );
  }
}