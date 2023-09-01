package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.configurate.ComponentSerializerType;
import net.forthecrown.utils.io.configurate.TomlConfigurationLoader;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2f;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector2l;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3f;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.math.vector.Vector3l;
import org.spongepowered.math.vector.Vector4d;
import org.spongepowered.math.vector.Vector4f;
import org.spongepowered.math.vector.Vector4i;
import org.spongepowered.math.vector.Vector4l;
import org.spongepowered.math.vector.VectorNd;
import org.spongepowered.math.vector.VectorNf;
import org.spongepowered.math.vector.VectorNi;
import org.spongepowered.math.vector.VectorNl;

public final class TomlConfigs {
  private TomlConfigs() {}

  public static <T> T loadPluginConfig(JavaPlugin plugin, Class<T> type) {
    return loadConfig("config.toml", plugin, type);
  }

  public static <T> T loadConfig(String configName, JavaPlugin plugin, Class<T> type) {
    Path path = PathUtil.pluginPath(plugin, configName);

    try {
      PathUtil.ensureParentExists(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    PluginJar.saveResources(plugin, configName);

    try {
      ConfigurationNode node = load(path);
      return node.get(type);
    } catch (ConfigurateException exc) {
      throw new RuntimeException(exc);
    }
  }

  public static ConfigurationNode load(Path path) throws ConfigurateException {
    var loader = createLoader(path);
    return loader.load();
  }

  public static TomlConfigurationLoader createLoader(Path path) {
    return TomlConfigurationLoader.builder()
        .path(path)
        .defaultOptions(configurationOptions -> {
          return configurationOptions.serializers(TomlConfigs::registerTypeSerializers);
        })
        .headerMode(HeaderMode.PRESERVE)
        .build();
  }

  private static void registerTypeSerializers(TypeSerializerCollection.Builder builder) {
    builder
        .registerAnnotatedObjects(
            ObjectMapper.factoryBuilder()
                .addNodeResolver((name, element) -> parent -> parent.node(name))
                .build()
        )

        .registerExact(Duration.class, createDurationSerializer())
        .registerExact(WorldVec3i.class, new WorldVec3iSerializer())

        .register(Component.class, new ComponentSerializerType())

        // Auto generated via a TypeScript script
        .registerExact(Vector2f.class, new Vector2fSerializer())
        .registerExact(Vector2d.class, new Vector2dSerializer())
        .registerExact(Vector2i.class, new Vector2iSerializer())
        .registerExact(Vector2l.class, new Vector2lSerializer())
        .registerExact(Vector3f.class, new Vector3fSerializer())
        .registerExact(Vector3d.class, new Vector3dSerializer())
        .registerExact(Vector3i.class, new Vector3iSerializer())
        .registerExact(Vector3l.class, new Vector3lSerializer())
        .registerExact(Vector4f.class, new Vector4fSerializer())
        .registerExact(Vector4d.class, new Vector4dSerializer())
        .registerExact(Vector4i.class, new Vector4iSerializer())
        .registerExact(Vector4l.class, new Vector4lSerializer())
        .registerExact(VectorNf.class, new VectorNfSerializer())
        .registerExact(VectorNd.class, new VectorNdSerializer())
        .registerExact(VectorNi.class, new VectorNiSerializer())
        .registerExact(VectorNl.class, new VectorNlSerializer());
  }

  public static Duration parseDuration(String strValue) throws CommandSyntaxException {
    StringReader reader = new StringReader(strValue);
    TimeArgument parser = ArgumentTypes.time();
    Duration result = parser.parse(reader);

    while (reader.canRead() && reader.peek() == ':') {
      reader.skip();

      Duration dur = parser.parse(reader);
      result = result.plus(dur);
    }

    return result;
  }

  public static TypeSerializer<Duration> createDurationSerializer() {
    return new TypeSerializer<>() {
      @Override
      public Duration deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var strValue = node.getString();

        if (strValue == null) {
          long longVal = node.getLong();

          if (longVal == ConfigurationNode.NUMBER_DEF) {
            throw new SerializationException("Don't know how to deserialize Duration");
          }

          return Duration.ofMillis(longVal);
        }

        try {
          return parseDuration(strValue);
        } catch (CommandSyntaxException exc) {
          throw new SerializationException(exc);
        }
      }

      @Override
      public void serialize(Type type, @Nullable Duration obj, ConfigurationNode node)
          throws SerializationException
      {
        if (obj == null) {
          node.set(null);
          return;
        }

        long millis = obj.toMillis();
        node.set(millis);
      }
    };
  }
}


class WorldVec3iSerializer implements TypeSerializer<WorldVec3i> {

  @Override
  public WorldVec3i deserialize(Type type, ConfigurationNode node) throws SerializationException {
    if (node.isNull()) {
      return null;
    }

    World world;

    if (node.hasChild("world")) {
      world = Bukkit.getWorld(node.node("world").getString());
    } else {
      world = null;
    }

    int x = node.node("x").getInt();
    int y = node.node("y").getInt();
    int z = node.node("z").getInt();

    return new WorldVec3i(world, x, y, z);
  }

  @Override
  public void serialize(Type type, @Nullable WorldVec3i obj, ConfigurationNode node)
      throws SerializationException
  {
    if (obj == null) {
      node.set(null);
      return;
    }

    if (obj.getWorld() != null) {
      node.node("world").set(obj.getWorld().getName());
    }

    node.node("x").set(obj.x());
    node.node("y").set(obj.y());
    node.node("z").set(obj.z());
  }
}

// Auto generated via a TypeScript script

class Vector2fSerializer implements TypeSerializer<Vector2f> {

  @Override
  public Vector2f deserialize(Type type, ConfigurationNode node) throws SerializationException {

    float x = node.node("x").getFloat();
    float y = node.node("y").getFloat();
    return new Vector2f(x, y);

  }

  @Override
  public void serialize(Type type, @Nullable Vector2f vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
  }
}

class Vector2dSerializer implements TypeSerializer<Vector2d> {

  @Override
  public Vector2d deserialize(Type type, ConfigurationNode node) throws SerializationException {

    double x = node.node("x").getDouble();
    double y = node.node("y").getDouble();
    return new Vector2d(x, y);

  }

  @Override
  public void serialize(Type type, @Nullable Vector2d vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
  }
}

class Vector2iSerializer implements TypeSerializer<Vector2i> {

  @Override
  public Vector2i deserialize(Type type, ConfigurationNode node) throws SerializationException {

    int x = node.node("x").getInt();
    int y = node.node("y").getInt();
    return new Vector2i(x, y);

  }

  @Override
  public void serialize(Type type, @Nullable Vector2i vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
  }
}

class Vector2lSerializer implements TypeSerializer<Vector2l> {

  @Override
  public Vector2l deserialize(Type type, ConfigurationNode node) throws SerializationException {

    long x = node.node("x").getLong();
    long y = node.node("y").getLong();
    return new Vector2l(x, y);

  }

  @Override
  public void serialize(Type type, @Nullable Vector2l vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
  }
}

class Vector3fSerializer implements TypeSerializer<Vector3f> {

  @Override
  public Vector3f deserialize(Type type, ConfigurationNode node) throws SerializationException {

    float x = node.node("x").getFloat();
    float y = node.node("y").getFloat();
    float z = node.node("z").getFloat();
    return new Vector3f(x, y, z);

  }

  @Override
  public void serialize(Type type, @Nullable Vector3f vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
  }
}

class Vector3dSerializer implements TypeSerializer<Vector3d> {

  @Override
  public Vector3d deserialize(Type type, ConfigurationNode node) throws SerializationException {

    double x = node.node("x").getDouble();
    double y = node.node("y").getDouble();
    double z = node.node("z").getDouble();
    return new Vector3d(x, y, z);

  }

  @Override
  public void serialize(Type type, @Nullable Vector3d vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
  }
}

class Vector3iSerializer implements TypeSerializer<Vector3i> {

  @Override
  public Vector3i deserialize(Type type, ConfigurationNode node) throws SerializationException {

    int x = node.node("x").getInt();
    int y = node.node("y").getInt();
    int z = node.node("z").getInt();
    return new Vector3i(x, y, z);

  }

  @Override
  public void serialize(Type type, @Nullable Vector3i vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
  }
}

class Vector3lSerializer implements TypeSerializer<Vector3l> {

  @Override
  public Vector3l deserialize(Type type, ConfigurationNode node) throws SerializationException {

    long x = node.node("x").getLong();
    long y = node.node("y").getLong();
    long z = node.node("z").getLong();
    return new Vector3l(x, y, z);

  }

  @Override
  public void serialize(Type type, @Nullable Vector3l vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
  }
}

class Vector4fSerializer implements TypeSerializer<Vector4f> {

  @Override
  public Vector4f deserialize(Type type, ConfigurationNode node) throws SerializationException {

    float x = node.node("x").getFloat();
    float y = node.node("y").getFloat();
    float z = node.node("z").getFloat();
    float w = node.node("w").getFloat();
    return new Vector4f(x, y, z, w);

  }

  @Override
  public void serialize(Type type, @Nullable Vector4f vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
    node.node("w").set(vec.w());
  }
}

class Vector4dSerializer implements TypeSerializer<Vector4d> {

  @Override
  public Vector4d deserialize(Type type, ConfigurationNode node) throws SerializationException {

    double x = node.node("x").getDouble();
    double y = node.node("y").getDouble();
    double z = node.node("z").getDouble();
    double w = node.node("w").getDouble();
    return new Vector4d(x, y, z, w);

  }

  @Override
  public void serialize(Type type, @Nullable Vector4d vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
    node.node("w").set(vec.w());
  }
}

class Vector4iSerializer implements TypeSerializer<Vector4i> {

  @Override
  public Vector4i deserialize(Type type, ConfigurationNode node) throws SerializationException {

    int x = node.node("x").getInt();
    int y = node.node("y").getInt();
    int z = node.node("z").getInt();
    int w = node.node("w").getInt();
    return new Vector4i(x, y, z, w);

  }

  @Override
  public void serialize(Type type, @Nullable Vector4i vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
    node.node("w").set(vec.w());
  }
}

class Vector4lSerializer implements TypeSerializer<Vector4l> {

  @Override
  public Vector4l deserialize(Type type, ConfigurationNode node) throws SerializationException {

    long x = node.node("x").getLong();
    long y = node.node("y").getLong();
    long z = node.node("z").getLong();
    long w = node.node("w").getLong();
    return new Vector4l(x, y, z, w);

  }

  @Override
  public void serialize(Type type, @Nullable Vector4l vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    node.node("x").set(vec.x());
    node.node("y").set(vec.y());
    node.node("z").set(vec.z());
    node.node("w").set(vec.w());
  }
}

class VectorNfSerializer implements TypeSerializer<VectorNf> {

  @Override
  public VectorNf deserialize(Type type, ConfigurationNode node) throws SerializationException {
    List<Float> list = node.getList(Float.class, List.of());
    if (list.isEmpty()) {
      return new VectorNf();
    }

    float[] arr = new float[list.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = list.get(i);
    }
    return new VectorNf(arr);
  }

  @Override
  public void serialize(Type type, @Nullable VectorNf vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }

    List<Float> list = new ArrayList<>(vec.size());
    for (int i = 0; i < vec.size(); i++) {
      list.add(vec.get(i));
    }
    node.setList(Float.class, list);
  }
}

class VectorNdSerializer implements TypeSerializer<VectorNd> {

  @Override
  public VectorNd deserialize(Type type, ConfigurationNode node) throws SerializationException {
    List<Double> list = node.getList(Double.class, List.of());
    if (list.isEmpty()) {
      return new VectorNd();
    }

    double[] arr = new double[list.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = list.get(i);
    }
    return new VectorNd(arr);
  }

  @Override
  public void serialize(Type type, @Nullable VectorNd vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }

    List<Double> list = new ArrayList<>(vec.size());
    for (int i = 0; i < vec.size(); i++) {
      list.add(vec.get(i));
    }
    node.setList(Double.class, list);
  }
}

class VectorNiSerializer implements TypeSerializer<VectorNi> {

  @Override
  public VectorNi deserialize(Type type, ConfigurationNode node) throws SerializationException {
    List<Integer> list = node.getList(Integer.class, List.of());
    if (list.isEmpty()) {
      return new VectorNi();
    }

    int[] arr = new int[list.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = list.get(i);
    }
    return new VectorNi(arr);
  }

  @Override
  public void serialize(Type type, @Nullable VectorNi vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }

    List<Integer> list = new ArrayList<>(vec.size());
    for (int i = 0; i < vec.size(); i++) {
      list.add(vec.get(i));
    }
    node.setList(Integer.class, list);
  }
}

class VectorNlSerializer implements TypeSerializer<VectorNl> {

  @Override
  public VectorNl deserialize(Type type, ConfigurationNode node) throws SerializationException {
    List<Long> list = node.getList(Long.class, List.of());
    if (list.isEmpty()) {
      return new VectorNl();
    }

    long[] arr = new long[list.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = list.get(i);
    }
    return new VectorNl(arr);
  }

  @Override
  public void serialize(Type type, @Nullable VectorNl vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }

    List<Long> list = new ArrayList<>(vec.size());
    for (int i = 0; i < vec.size(); i++) {
      list.add(vec.get(i));
    }
    node.setList(Long.class, list);
  }
}

// Generation script
/*
interface VectorType {
  primitive: string;
  object: string;
}

const N_DIM = 999;

const DIMENSIONS = [ 2, 3, 4, N_DIM  ];
const DIM_NAMES  = [ 'x', 'y', 'z', 'w' ];

const TYPE_NAMES = {
  f: {
    primitive: 'float',
    object: 'Float'
  },
  d: {
    primitive: 'double',
    object: 'Double'
  },
  i: {
    primitive: 'int',
    object: 'Integer'
  },
  l: {
    primitive: 'long',
    object: 'Long'
  }
}

let out: string = "";
let registerMethods: string[] = [];

for (let dimIndex = 0; dimIndex < DIMENSIONS.length; dimIndex++) {
  let dimension = DIMENSIONS[dimIndex];

  for (const suffix in TYPE_NAMES) {
    let vectorType: any = TYPE_NAMES[suffix];
    genVector(dimension, suffix, vectorType);
  }
}

console.log(out);
console.log(registerMethods.join("\n"));

function genVector(dimensions: number, suffix: string, vType: VectorType) {
  let dimensionsString = dimensions == N_DIM ? "N" : dimensions.toString();
  let vectorType = `Vector${dimensionsString}${suffix}`;

  let serializeBody = "";
  let deserializeBody = "";

  let className = `${vectorType}Serializer`;
  let reg = `//builder.registerExact(${vectorType}.class, new ${className}());`
  registerMethods.push(reg);

  if (dimensions == N_DIM) {
    deserializeBody =
`List<${vType.object}> list = node.getList(${vType.object}.class, List.of());
    if (list.isEmpty()) {
      return new ${vectorType}();
    }

    ${vType.primitive}[] arr = new ${vType.primitive}[list.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = list.get(i);
    }
    return new ${vectorType}(arr);`

    serializeBody = `
    List<${vType.object}> list = new ArrayList<>(vec.size());
    for (int i = 0; i < vec.size(); i++) {
      list.add(vec.get(i));
    }
    node.setList(${vType.object}.class, list);`
  } else {
    let prim = vType.primitive;
    let methodSuffix = prim.substring(0, 1).toUpperCase() + prim.substring(1);

    let getters: string[] = Array(dimensions);
    let setters: string[] = Array(dimensions);
    let ctor: string[] = Array(dimensions);

    for (let i = 0; i < dimensions; i++) {
      let name = DIM_NAMES[i];
      getters[i] = `${vType.primitive} ${name} = node.node("${name}").get${methodSuffix}();`;
      setters[i] = `node.node("${name}").set(vec.${name}());`
      ctor[i] = name;
    }

    let getterString = getters.join("\n    ");
    let setterString = setters.join("\n    ");
    let ctorString = ctor.join(", ");

    deserializeBody = `
${getterString}
return new ${vectorType}(${ctorString});
    `

    serializeBody = setterString
  }

out += `
class ${className} implements TypeSerializer<${vectorType}> {

  @Override
  public ${vectorType} deserialize(Type type, ConfigurationNode node) throws SerializationException {
    ${deserializeBody}
  }

  @Override
  public void serialize(Type type, @Nullable ${vectorType} vec, ConfigurationNode node) throws SerializationException {
    if (vec == null) {
      node.set(null);
      return;
    }
    ${serializeBody}
  }
}
`
}
 */