package net.forthecrown.scripts;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import net.forthecrown.classloader.Libraries;
import org.jetbrains.annotations.NotNull;

public class ScriptPluginLoader implements PluginLoader {

  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    MavenLibraryResolver resolver = Libraries.loadResolver(getClass().getClassLoader());
    classpathBuilder.addLibrary(resolver);
  }
}
