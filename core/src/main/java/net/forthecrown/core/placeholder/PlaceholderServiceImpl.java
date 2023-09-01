package net.forthecrown.core.placeholder;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.registry.Registries;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.PlaceholderService;
import net.forthecrown.text.placeholder.PlaceholderSource;
import net.forthecrown.text.placeholder.PlayerPlaceholders;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
public class PlaceholderServiceImpl implements PlaceholderService {

  private final CorePlugin plugin;

  private final ObjectList<PlaceholderSource> defaultSources = new ObjectArrayList<>();
  private final PlaceholderListImpl defaults = new PlaceholderListImpl();

  public PlaceholderServiceImpl(CorePlugin plugin) {
    this.plugin = plugin;

    defaultSources.add(defaults);
    defaultSources.add(new PlayerPlaceholders("viewer", null));

    defaults.add("text", new ComponentPlaceholder());
  }

  @Override
  public void addDefaultSource(PlaceholderSource source) {
    Objects.requireNonNull(source, "Null source");
    defaultSources.add(source);
  }

  @Override
  public void removeDefaultSource(PlaceholderSource source) {
    defaultSources.remove(source);
  }

  @Override
  public PlaceholderListImpl newList() {
    return new PlaceholderListImpl();
  }

  @Override
  public PlaceholderRendererImpl newRenderer() {
    return new PlaceholderRendererImpl(this);
  }

  public void load() {
    var path = PathUtil.pluginPath("placeholders.json");
    PluginJar.saveResources("placeholders.json", path);

    var logger = Loggers.getLogger();

    SerializationHelper.readAsJson(path, json -> {
      for (Entry<String, JsonElement> entry : json.entrySet()) {
        String key = entry.getKey();

        if (!Registries.isValidKey(key)) {
          logger.error("Cannot load placeholder '{}': Invalid key", key);
          continue;
        }

        try {
          ViewerAwareMessage message = JsonUtils.readMessage(entry.getValue());
          TextPlaceholder placeholder = new LoadedPlaceholder(message, true);

          defaults.add(key, placeholder);

          logger.debug("Loaded placeholder {}", key);
        } catch (Throwable t) {
          logger.error("Cannot load placeholder '{}'", key, t);
        }
      }
    });
  }

  private record LoadedPlaceholder(ViewerAwareMessage message, boolean subRendering)
      implements TextPlaceholder
  {

    @Override
    public @Nullable Component render(String match, PlaceholderContext render) {
      Component text = message.create(render.viewer());

      return subRendering
          ? render.renderer().render(text, render.viewer(), render.context())
          : text;
    }
  }
}
