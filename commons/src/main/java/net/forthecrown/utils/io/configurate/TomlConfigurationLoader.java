package net.forthecrown.utils.io.configurate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.UnmodifiableCollections;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseError;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

public class TomlConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

  private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
      List.class,
      Map.class,
      Double.class,
      Float.class,
      Long.class,
      Integer.class,
      Boolean.class,
      String.class,
      LocalTime.class,
      LocalDate.class,
      LocalDateTime.class,
      OffsetDateTime.class
  );

  static final ConfigurationOptions DEFAULT_OPTIONS = ConfigurationOptions.defaults()
      .nativeTypes(NATIVE_TYPES)
      .serializers(TomlTypes.SERIALIZERS);

  public TomlConfigurationLoader(Builder<?, ?> builder) {
    super(builder, new CommentHandler[]{CommentHandlers.HASH});
  }

  public static TomlBuilder builder() {
    return new TomlBuilder();
  }

  @Override
  protected void loadInternal(BasicConfigurationNode node, BufferedReader reader)
      throws ParsingException
  {
    TomlParseResult result;

    try {
      result = Toml.parse(reader);
    } catch (IOException exc) {
      throw ParsingException.wrap(node, exc);
    }

    if (result.hasErrors()) {
      TomlParseError error = result.errors().get(0);
      var pos = error.position();
      throw new ParsingException(node, pos.line(), pos.column(), null, error.getMessage(), error);
    }

    loadObject(result, node);
  }

  private void loadObject(Object o, ConfigurationNode dest) {
    if (o instanceof TomlTable table) {
      var entries = table.entrySet();

      for (var e: entries) {
        ConfigurationNode node = dest.node(e.getKey());
        loadObject(e.getValue(), node);
      }

      return;
    }

    if (o instanceof TomlArray arr) {
      for (var e: arr.toList()) {
        ConfigurationNode node = dest.appendListNode();
        loadObject(e, node);
      }

      return;
    }

    dest.raw(o);
  }

  @Override
  protected void saveInternal(ConfigurationNode node, Writer writer) throws ConfigurateException {
    try {
      TomlSerializer.write(node, writer);
    } catch (IOException exc) {
      throw ConfigurateException.wrap(node, exc);
    }
  }

  @Override
  public BasicConfigurationNode createNode(ConfigurationOptions options) {
    return BasicConfigurationNode.root(options.nativeTypes(NATIVE_TYPES));
  }

  public static class TomlBuilder extends Builder<TomlBuilder, TomlConfigurationLoader> {

    public TomlBuilder() {
      this.defaultOptions(DEFAULT_OPTIONS);
    }

    @Override
    public TomlConfigurationLoader build() {
      return new TomlConfigurationLoader(this);
    }
  }
}
