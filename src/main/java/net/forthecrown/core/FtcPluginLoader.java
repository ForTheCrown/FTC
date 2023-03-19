package net.forthecrown.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.forthecrown.utils.io.JsonWrapper;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class FtcPluginLoader implements PluginLoader {

  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    MavenLibraryResolver resolver = new MavenLibraryResolver();
    var loader = getClass().getClassLoader();
    var resource = loader.getResourceAsStream("runtime_dependencies.json");

    if (resource == null) {
      classpathBuilder.getContext()
          .getLogger()
          .warn("No runtime_dependencies.json found");

      return;
    }

    JsonObject obj = JsonParser.parseReader(
        new InputStreamReader(resource, StandardCharsets.UTF_8)
    ).getAsJsonObject();

    JsonArray repos = obj.getAsJsonArray("repositories");
    JsonArray depends = obj.getAsJsonArray("dependencies");

    repos.forEach(element -> {
      JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

      String type = json.getString("type");
      String id = json.getString("id");
      String url = json.getString("url");

      Objects.requireNonNull(id, "ID not set for repo");
      Objects.requireNonNull(url, "URL not set for '" + id + "'");

      RemoteRepository repository = new RemoteRepository.Builder(id, type, url)
          .build();

      resolver.addRepository(repository);
    });

    depends.forEach(element -> {
      resolver.addDependency(
          new Dependency(new DefaultArtifact(element.getAsString()), null)
      );
    });

    classpathBuilder.addLibrary(resolver);
  }


}