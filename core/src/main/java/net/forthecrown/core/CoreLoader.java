package net.forthecrown.core;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import net.forthecrown.classloader.Libraries;
import org.jetbrains.annotations.NotNull;

public class CoreLoader implements PluginLoader {

  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    var resolver = Libraries.loadResolver(getClass().getClassLoader());
    classpathBuilder.addLibrary(resolver);
  }
}