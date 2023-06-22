package net.forthecrown.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.forthecrown.utils.io.JsonWrapper;
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

  public static MavenLibraryResolver loadResolver(ClassLoader loader) {
    return loadResolver(loader, DEFAULT_RESOURCE_NAME);
  }

  public static MavenLibraryResolver loadResolver(ClassLoader loader, String resourceName) {
    MavenLibraryResolver resolver = new MavenLibraryResolver();

    var stream = loader.getResourceAsStream(resourceName);

    if (stream == null) {
      return resolver;
    }

    JsonWrapper json;
    try (var reader = new InputStreamReader(stream)) {
      var obj = JsonParser.parseReader(reader).getAsJsonObject();
      json = JsonWrapper.wrap(obj);
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }

    if (json.has(KEY_REPOSITORIES)) {
      JsonObject obj = json.getObject(KEY_REPOSITORIES);

      for (var e: obj.entrySet()) {
        String id = e.getKey();
        String url = e.getValue().getAsString();

        RemoteRepository repo = new RemoteRepository.Builder(id, "default", url)
            .build();

        resolver.addRepository(repo);
      }
    }

    if (json.has(KEY_DEPENDENCIES)) {
      JsonArray arr = json.getArray(KEY_DEPENDENCIES);

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

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    String artifactString = json.getString("artifact");
    Objects.requireNonNull(artifactString, "No artifact string");

    Artifact artifact = new DefaultArtifact(artifactString);
    String scope = json.getString("scope", null);
    Boolean optional;

    if (json.has("optional")) {
      optional = json.getBool("optional");
    } else {
      optional = null;
    }

    List<Exclusion> exclusions;

    if (json.has("exclude")) {
      exclusions = new ArrayList<>();
      JsonArray arr = json.getArray("exclude");

      for (var e: arr) {
         Exclusion exclusion;

        if (e.isJsonPrimitive()) {
          exclusion = new Exclusion(null, e.getAsString(), null, null);
        } else {
          JsonWrapper excl = JsonWrapper.wrap(e.getAsJsonObject());

          String groupId = excl.getString("groupId", null);
          String artifactId = excl.getString("artifactId", null);
          String classifier = excl.getString("classifier", null);
          String extension = excl.getString("extension", null);

          exclusion = new Exclusion(groupId, artifactId, classifier, extension);
        }

        exclusions.add(exclusion);
      }
    } else {
      exclusions = null;
    }

    return new Dependency(artifact, scope, optional, exclusions);
  }
}