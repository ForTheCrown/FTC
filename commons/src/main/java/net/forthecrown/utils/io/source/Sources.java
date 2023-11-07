package net.forthecrown.utils.io.source;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.utils.io.TagOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Sources {
  private Sources() {}

  public static final Charset CHARSET = StandardCharsets.UTF_8;

  public static Source fromPath(@NotNull Path path) {
    return fromPath(path, null);
  }

  public static Source fromPath(@NotNull Path path, @Nullable Path parentDirectory) {
    Objects.requireNonNull(path, "Null path");

    String name;

    if (parentDirectory == null) {
      name = path.toString();
    } else {
      Path relative = parentDirectory.relativize(path);
      name = relative.toString();
    }

    return new PathSource(path, parentDirectory, name);
  }

  public static Source fromUrl(@NotNull String url) throws MalformedURLException {
    Objects.requireNonNull(url, "Null url");
    URL urlObject = new URL(url);
    return fromUrl(urlObject);
  }

  public static Source fromUrl(@NotNull String url, String name) throws MalformedURLException {
    Objects.requireNonNull(url, "Null url");
    URL urlObject = new URL(url);
    return fromUrl(urlObject, name);
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

  public static <S> Source load(Dynamic<S> dynamic, Path directory, boolean assumeDirect) {
    String strValue = dynamic.asString(null);

    if (strValue != null) {
      return assumeDirect ? direct(strValue) : fromPath(directory.resolve(strValue), directory);
    }

    String name = dynamic.get("name")
        .map(dynamic1 -> dynamic1.asString(null))
        .result().orElse(null);

    var raw = dynamic.get("raw").asString(null);
    var js = dynamic.get("js").asString(null);
    var url = dynamic.get("url").asString(null);
    var path = dynamic.get("path").asString(null);

    if (raw != null) {
      return direct(raw, name);
    }

    if (js != null) {
      return direct(js, name);
    }

    if (path != null) {
      return fromPath(directory.resolve(path), directory);
    }

    if (url != null) {
      try {
        return fromUrl(url, name);
      } catch (MalformedURLException exc) {
        throw new IllegalStateException(exc);
      }
    }

    throw new IllegalArgumentException(
        "Invalid ScriptSource object: " + dynamic.getValue() + ". "
        + "Requires 1 of 3 values: "
            + " \n'path' for file sources, "
            + " \n'url' for sources that use a URL and "
            + " \n'raw' for sources that are a direct value"
    );
  }
}