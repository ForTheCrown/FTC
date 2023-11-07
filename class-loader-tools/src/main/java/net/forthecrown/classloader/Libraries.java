package net.forthecrown.classloader;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;

public final class Libraries {
  private Libraries() {}

  public static final String DEFAULT_RESOURCE_NAME = "runtime_libraries.json";

  private static final String KEY_REPOSITORIES = "repositories";
  private static final String KEY_DEPENDENCIES = "dependencies";

  private static final String CENTRAL_URL = "https://repo1.maven.org/maven2/";

  public static MavenLibraryResolver loadResolver(ClassLoader loader) {
    return loadResolver(loader, DEFAULT_RESOURCE_NAME);
  }

  public static MavenLibraryResolver loadResolver(ClassLoader loader, String resourceName) {
    MavenLibraryResolver resolver = new MavenLibraryResolver();

    var stream = loader.getResourceAsStream(resourceName);

    if (stream == null) {
      return resolver;
    }

    JsonObject json;
    try (var reader = new InputStreamReader(stream)) {
      json = JsonParser.parseReader(reader).getAsJsonObject();
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }

    boolean centralAdded = false;

    if (json.has(KEY_REPOSITORIES)) {
      JsonObject obj = json.getAsJsonObject(KEY_REPOSITORIES);

      for (var e: obj.entrySet()) {
        String id = e.getKey();
        String url = e.getValue().getAsString();

        if (url.equals(CENTRAL_URL)) {
          centralAdded = true;
        }

        RemoteRepository repo = new RemoteRepository.Builder(id, "default", url)
            .build();

        resolver.addRepository(repo);
      }
    }

    if (!centralAdded) {
      centralAdded = true;

      RemoteRepository repo = new RemoteRepository.Builder("central", "default", CENTRAL_URL)
          .build();

      resolver.addRepository(repo);
    }

    if (json.has(KEY_DEPENDENCIES)) {
      JsonArray arr = json.getAsJsonArray(KEY_DEPENDENCIES);

      for (var e: arr) {
        var dep = loadDependency(e);
        resolver.addDependency(dep);
      }
    }

    return resolver;
  }

  private static Dependency loadDependency(JsonElement element) {
    if (element.isJsonPrimitive()) {
      String str = element.getAsString();
      DefaultArtifact artifact = new DefaultArtifact(str);
      return new Dependency(artifact, null);
    }

    JsonObject json = element.getAsJsonObject();

    Artifact artifact = loadArtifact(json.get("artifact"));
    String scope = getString(json, "scope");
    Boolean optional;

    if (json.has("optional")) {
      optional = getBool(json, "optional");
    } else {
      optional = null;
    }

    List<Exclusion> exclusions;

    if (json.has("exclude")) {
      exclusions = new ArrayList<>();
      JsonArray arr = json.getAsJsonArray("exclude");

      for (var e: arr) {
         Exclusion exclusion;

        if (e.isJsonPrimitive()) {
          exclusion = new Exclusion(null, e.getAsString(), null, null);
        } else {
          JsonObject excl = e.getAsJsonObject();

          String groupId = getString(excl, "groupId");
          String artifactId = getString(excl, "artifactId");
          String classifier = getString(excl, "classifier");
          String extension = getString(excl, "extension");

          exclusion = new Exclusion(groupId, artifactId, classifier, extension);
        }

        exclusions.add(exclusion);
      }
    } else {
      exclusions = null;
    }

    return new Dependency(artifact, scope, optional, exclusions);
  }

  private static Artifact loadArtifact(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      throw new NullPointerException("'artifact' element is null");
    }

    if (element.isJsonPrimitive()) {
      String s = element.getAsString();
      return new DefaultArtifact(s);
    }

    if (!element.isJsonObject()) {
      throw new IllegalStateException("Don't know how to load artifact from " + element);
    }

    JsonObject obj = element.getAsJsonObject();
    Preconditions.checkState(obj.size() == 1, "Only 1 entry allowed in artifact object");

    JsonObject osBased = obj.getAsJsonObject("os_based");
    if (osBased != null && !osBased.isJsonNull()) {
      OperatingSystem system = operatingSystem();
      JsonElement value = osBased.get(system.name().toLowerCase());

      Preconditions.checkState(
          value != null && value.isJsonPrimitive(),
          "Cannot read OS value: %s",
          value
      );

      String s = value.getAsString();
      return new DefaultArtifact(s);
    }

    throw new IllegalStateException("Don't know how load artifact from " + element);
  }

  private static String getString(JsonObject obj, String key) {
    if (!obj.has(key)) {
      return null;
    }

    JsonElement el = obj.get(key);

    if (!el.isJsonPrimitive()) {
      return null;
    }

    return el.getAsString();
  }

  private static boolean getBool(JsonObject obj, String key) {
    if (!obj.has(key)) {
      return false;
    }

    JsonElement el = obj.get(key);

    if (!el.isJsonPrimitive()) {
      return false;
    }

    return el.getAsBoolean();
  }

  private static OperatingSystem operatingSystem() {
    if (SystemUtils.IS_OS_LINUX) {
      return OperatingSystem.LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return OperatingSystem.MAC;
    } else {
      return OperatingSystem.WINDOWS;
    }
  }

  enum OperatingSystem {
    LINUX, WINDOWS, MAC
  }
}