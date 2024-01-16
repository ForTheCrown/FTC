package net.forthecrown.scripts.modules;

import static org.mozilla.javascript.Context.javaToJS;

import com.destroystokyo.paper.ParticleBuilder;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.scripts.ScriptUtils;
import net.forthecrown.scripts.module.JsModule;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.spongepowered.math.GenericMath;

public class ParticlesModule extends IdScriptableObject {

  static final double DEFAULT_INTERVAL = 0.5d;

  static final int ID_spawnParticle = 1;
  static final int ID_spawnParticleLine = 2;
  static final int ID_spawnParticleCircle = 3;
  static final int ID_spawnParticleSphere = 4;
  static final int ID_compileOptions = 5;

  static final int MAX_ID = ID_compileOptions;

  static final String NAME_spawnParticle = "spawnParticle";
  static final String NAME_spawnParticleLine = "spawnParticleLine";
  static final String NAME_spawnParticleCircle = "spawnParticleCircle";
  static final String NAME_spawnParticleSphere = "spawnParticleSphere";
  static final String NAME_compileOptions = "compileOptions";

  public static final JsModule MODULE = scope -> {
    ParticlesModule module = new ParticlesModule();
    module.activatePrototypeMap(MAX_ID);
    module.setParentScope(scope);
    return module;
  };

  @Override
  public String getClassName() {
    return "Particles";
  }

  @Override
  protected void initPrototypeId(int id) {
    String name = getInstanceIdName(id);
    int arity = switch (id) {
      case ID_spawnParticle -> 3;
      case ID_spawnParticleLine -> 4;
      case ID_spawnParticleCircle -> 4;
      case ID_spawnParticleSphere -> 4;
      case ID_compileOptions -> 1;
      default -> 0;
    };

    initPrototypeMethod(getClassName(), id, name, arity);
  }

  @Override
  protected String getInstanceIdName(int id) {
    return switch (id) {
      case ID_spawnParticle -> NAME_spawnParticle;
      case ID_spawnParticleLine -> NAME_spawnParticleLine;
      case ID_spawnParticleCircle -> NAME_spawnParticleCircle;
      case ID_spawnParticleSphere -> NAME_spawnParticleSphere;
      case ID_compileOptions -> NAME_compileOptions;
      default -> throw new IllegalStateException(String.valueOf(id));
    };
  }

  @Override
  protected int findPrototypeId(String name) {
    return switch (name) {
      case NAME_spawnParticle -> ID_spawnParticle;
      case NAME_spawnParticleLine -> ID_spawnParticleLine;
      case NAME_spawnParticleCircle -> ID_spawnParticleCircle;
      case NAME_spawnParticleSphere -> ID_spawnParticleSphere;
      case NAME_compileOptions -> ID_compileOptions;
      default -> throw new IllegalStateException(name);
    };
  }

  @Override
  public Object execIdCall(
      IdFunctionObject f,
      Context cx,
      Scriptable scope,
      Scriptable thisObj,
      Object[] args
  ) {
    switch (f.methodId()) {
      case ID_spawnParticle -> {
        jsSpawnParticle(args, cx);
        return Context.getUndefinedValue();
      }

      case ID_spawnParticleLine -> {
        jsParticleLine(args, cx);
        return Context.getUndefinedValue();
      }

      case ID_spawnParticleCircle -> {
        jsParticleCircle(args, cx);
        return Context.getUndefinedValue();
      }

      case ID_spawnParticleSphere -> {
        jsParticleSphere(args, cx);
        return Context.getUndefinedValue();
      }

      case ID_compileOptions -> {
        ParticleSpawn spawn;

        if (args.length > 0) {
          spawn = ParticleSpawn.valueOf(args[0], cx);
        } else {
          spawn = new ParticleSpawn();
        }

        return javaToJS(spawn, scope);
      }

      default -> throw f.unknown();
    }
  }

  private static Vector2d toRadius2(Object o) {
    if (o instanceof Number number) {
      return new Vector2d(number.doubleValue());
    }
    return ScriptUtils.toVec2d(o);
  }

  private static Vector3d toRadius3(Object o) {
    if (o instanceof Number number) {
      return new Vector3d(number.doubleValue());
    }
    return ScriptUtils.toVec3d(o);
  }

  private static double getInterval(Object[] args, int index) {
    if (args.length <= index) {
      return DEFAULT_INTERVAL;
    }
    return ScriptRuntime.toNumber(args[index]);
  }

  private static void jsSpawnParticle(Object[] args, Context cx) {
    ScriptUtils.ensureParameterCount(args, 3);

    ParticleSpawn spawn = ParticleSpawn.valueOf(args[0], cx);
    World world = ScriptUtils.toWorld(args, 1);
    Vector3d pos = ScriptUtils.toVec3d(args, 2);

    var builder = spawn.createBuilder();
    spawn.applyBuilder(builder, 0f);

    builder
        .location(world, pos.x, pos.y, pos.z)
        .allPlayers()
        .spawn();
  }

  private static void jsParticleLine(Object[] args, Context cx) {
    ScriptUtils.ensureParameterCount(args, 4);

    ParticleSpawn spawn = ParticleSpawn.valueOf(args[0], cx);
    World world = ScriptUtils.toWorld(args[1]);
    Vector3d start = ScriptUtils.toVec3d(args[2]);
    Vector3d end = ScriptUtils.toVec3d(args[3]);
    double interval = getInterval(args, 4);

    Vector3d dif = new Vector3d();
    Vector3d point = new Vector3d(start);

    end.sub(start, dif);

    double len = dif.length();
    double points = len / interval;

    dif.div(points);

    ParticleBuilder builder = spawn.createBuilder();

    for (double i = 0; i < points; i++) {
      point.add(dif);

      double prog = i / points;

      spawn.applyBuilder(builder, prog);

      builder.location(world, point.x, point.y, point.z)
          .allPlayers()
          .spawn();
    }
  }

  private static void jsParticleCircle(Object[] args, Context cx) {
    ScriptUtils.ensureParameterCount(args, 4);

    ParticleSpawn spawn = ParticleSpawn.valueOf(args[0], cx);
    World world = ScriptUtils.toWorld(args[1]);
    Vector3d center = ScriptUtils.toVec3d(args[2]);
    Vector2d radius = toRadius2(args[3]);
    double interval = getInterval(args, 4);

    double circumference = ellipseCircumference(radius.x, radius.y);
    double pointCount = circumference / interval;

    Vector3d pos = new Vector3d();
    pos.y = center.y;

    var builder = spawn.createBuilder();

    for (double i = 0; i < pointCount; i++) {
      double prog = i / pointCount;
      double angle = 2 * Math.PI * prog;

      pos.x = center.x + radius.x * Math.cos(angle);
      pos.z = center.z + radius.y * Math.sin(angle);

      spawn.applyBuilder(builder, prog);

      builder
          .location(world, pos.x, pos.y, pos.z)
          .allPlayers()
          .spawn();
    }
  }

  private static double ellipseCircumference(double a, double b) {
    return Math.PI * Math.sqrt(2 * (a * a + b * b) - Math.pow(a - b, 2));
  }

  private static void jsParticleSphere(Object[] args, Context cx) {
    ScriptUtils.ensureParameterCount(args, 4);

    ParticleSpawn spawn = ParticleSpawn.valueOf(args[0], cx);
    World world = ScriptUtils.toWorld(args[1]);
    Vector3d center = ScriptUtils.toVec3d(args[2]);
    Vector3d radius = toRadius3(args[3]);

    double interval;

    if (args.length >= 5) {
      interval = ScriptRuntime.toNumber(args[4]);
    } else {
      interval = DEFAULT_INTERVAL;
    }

    double numPointsTheta = 2 * Math.PI * radius.x / interval;
    double numPointsPhi = Math.PI * radius.y / interval;

    var builder = spawn.createBuilder();

    for (double i = 0; i < numPointsTheta; i++) {
      double theta = i * (2 * Math.PI) / numPointsTheta;

      for (double j = 0; j < numPointsPhi; j++) {
        double phi = j * Math.PI / numPointsPhi;

        double x = center.x + radius.x * Math.sin(phi) * Math.cos(theta);
        double y = center.y + radius.y * Math.sin(phi) * Math.sin(theta);
        double z = center.z + radius.z * Math.cos(phi);

        double completion = (i * numPointsPhi + j) / (numPointsTheta * numPointsPhi);

        spawn.applyBuilder(builder, completion);
        builder.location(world, x, y, z)
            .allPlayers()
            .spawn();
      }
    }
  }

  @Getter @Setter
  public static final class ParticleSpawn {

    private Particle particle;
    private Vector3d offset;
    private int count = 0;
    private boolean force = false;
    private Float size;
    private ColorSupplier color;
    private ColorSupplier colorTransition;
    private ItemStack item;
    private BlockData blockData;
    private Float sculkCharge;
    private Integer shriek;

    public void setParticle(Object particle) {
      this.particle = getParticle(particle);
    }

    public void setItem(Object item) {
      this.item = ScriptUtils.toItemStack(item);
    }

    public void setOffset(Object offset) {
      this.offset = ScriptUtils.toVec3d(offset);
    }

    public void setBlockData(Object blockData) {
      this.blockData = ScriptUtils.toBlockData(blockData);
    }

    static Particle getParticle(Object o) {
      o = Context.jsToJava(o, Object.class);
      if (o instanceof Particle particle) {
        return particle;
      }

      NamespacedKey particleKey;

      if (o instanceof NamespacedKey namespacedKey) {
        particleKey = namespacedKey;
      } else if (o instanceof Key key) {
        particleKey = new NamespacedKey(key.namespace(), key.value());
      } else if (o instanceof CharSequence sequence) {
        particleKey = NamespacedKey.fromString(sequence.toString().toLowerCase());
        if (particleKey == null) {
          throw ScriptRuntime.typeError("Unknown particle: '" + sequence + "'");
        }
      } else {
        throw ScriptRuntime.typeError("Don't know how to load particle from: " + o);
      }

      Particle particle = Registry.PARTICLE_TYPE.get(particleKey);

      if (particle == null) {
        throw ScriptRuntime.typeError("Unknown particle: " + particleKey);
      }

      return particle;
    }

    private static ColorSupplier getColor(Object o, Context cx, Scriptable scope) {
      o = Context.jsToJava(o, Object.class);

      if (o instanceof Callable callable) {
        return new ScriptCallableColor(callable, cx, scope);
      }

      if (o instanceof NativeArray arr) {
        if (arr.isEmpty()) {
          throw ScriptRuntime.typeError("Empty color array");
        }

        Color[] colors = new Color[arr.size()];
        for (int i = 0; i < colors.length; i++) {
          colors[i] = getColorLiteral(arr.get(i));
        }
        return new LerpColor(colors);
      }

      Color literal = getColorLiteral(o);
      return new ConstColor(literal);
    }

    private static ColorSupplier getColor(Scriptable scriptable, String key, Context cx) {
      Object o = getProperty(scriptable, key);

      if (o == NOT_FOUND) {
        return null;
      }

      return getColor(o, cx, scriptable);
    }

    private static Color getColorLiteral(Object o) {
      o = Context.jsToJava(o, Object.class);

      if (o instanceof Number number) {
        return Color.fromARGB(number.intValue());
      }

      if (o instanceof Vector3d vec3) {
        return Color.fromRGB((int) (255.0 * vec3.x), (int) (255.0 * vec3.y),
            (int) (255.0 * vec3.z));
      }

      if (o instanceof CharSequence sequence) {
        TextColor color = NamedTextColor.NAMES.value(sequence.toString());

        if (color == null) {
          throw ScriptRuntime.typeError("Unknown color: '" + sequence + "'");
        }

        return Color.fromRGB(color.red(), color.green(), color.blue());
      }

      if (o instanceof Scriptable scriptable) {
        int r = ScriptRuntime.toInt32(getProperty(scriptable, "red"));
        int g = ScriptRuntime.toInt32(getProperty(scriptable, "green"));
        int b = ScriptRuntime.toInt32(getProperty(scriptable, "blue"));
        return Color.fromRGB(r, g, b);
      }

      throw ScriptRuntime.typeError("Don't know how to read color value from: " + o);
    }

    public static ParticleSpawn valueOf(Object o, Context cx) {
      o = Context.jsToJava(o, Object.class);

      if (o instanceof ParticleSpawn spawn) {
        return spawn;
      }

      if (!(o instanceof Scriptable scriptable)) {
        ParticleSpawn spawn = new ParticleSpawn();
        spawn.setParticle(o);
        return spawn;
      }

      Object particleName = getProperty(scriptable, "particleName");

      if (particleName == NOT_FOUND) {
        throw ScriptRuntime.typeError("'particleName' property not found");
      }

      ParticleSpawn spawn = new ParticleSpawn();
      spawn.particle = getParticle(particleName);

      if (hasProperty(scriptable, "force")) {
        spawn.force = ScriptRuntime.toBoolean(getProperty(scriptable, "force"));
      }

      if (hasProperty(scriptable, "count")) {
        spawn.count = ScriptRuntime.toInt32(getProperty(scriptable, "count"));
      }

      if (hasProperty(scriptable, "offset")) {
        Object offsetObject = getProperty(scriptable, "offset");
        spawn.setOffset(offsetObject);
      }

      if (hasProperty(scriptable, "size")) {
        spawn.size = (float) ScriptRuntime.toNumber(getProperty(scriptable, "size"));
      }

      spawn.color = getColor(scriptable, "color", cx);
      spawn.colorTransition = getColor(scriptable, "colorTransition", cx);

      if (hasProperty(scriptable, "item")) {
        Object itemObj = getProperty(scriptable, "item");
        spawn.setItem(itemObj);
      }

      if (hasProperty(scriptable, "blockData")) {
        Object dataObj = getProperty(scriptable, "blockData");
        spawn.setBlockData(dataObj);
      }

      if (hasProperty(scriptable, "shriek")) {
        spawn.shriek = ScriptRuntime.toInt32(getProperty(scriptable, "shriek"));
      }

      if (hasProperty(scriptable, "sculkCharge")) {
        spawn.sculkCharge = (float) ScriptRuntime.toNumber(getProperty(scriptable, "sculkCharge"));
      }

      return spawn;
    }

    ParticleBuilder createBuilder() {
      ParticleBuilder builder = particle.builder();
      builder.force(force);

      if (offset != null) {
        builder.offset(offset.x, offset.y, offset.z);
      }
      if (count > 0) {
        builder.count(count);
      }

      if (item != null) {
        builder.data(item.clone());
      } else if (blockData != null) {
        builder.data(blockData.clone());
      } else if (shriek != null) {
        builder.data(shriek);
      } else if (sculkCharge != null) {
        builder.data(sculkCharge);
      }

      return builder;
    }

    void applyBuilder(ParticleBuilder builder, double completion) {
      completion = GenericMath.clamp(completion, 0.0, 1.0);

      if (color != null) {
        float size = Objects.requireNonNullElse(this.size, 1.0f);
        Color color = this.color.getColor(completion);

        if (colorTransition != null) {
          Color colorTransition = this.colorTransition.getColor(completion);
          builder.data(new DustTransition(color, colorTransition, size));
        } else {
          builder.data(new DustOptions(color, size));
        }
      }
    }
  }

  public interface ColorSupplier {
    Color getColor(double completion);
  }

  record LerpColor(Color[] colors) implements ColorSupplier {

    @Override
    public Color getColor(double completion) {
      if (colors.length == 1) {
        return colors[0];
      }
      if (colors.length == 2) {
        return lerp2(colors[0], colors[1], completion);
      }

      final int maxIndex = colors.length - 1;

      int firstIndex = (int) (completion * maxIndex);
      double firstStep = (double) firstIndex / maxIndex;
      double localStep = (completion - firstStep) * maxIndex;

      Color c1 = colors[firstIndex];
      Color c2 = colors[firstIndex + 1];

      return lerp2(c1, c2, localStep);
    }

    Color lerp2(Color c1, Color c2, double p) {
      return Color.fromARGB(
          (int) (c1.getAlpha() + p * (c2.getAlpha() - c1.getAlpha())),
          (int) (c1.getRed()   + p * (c2.getRed()   - c1.getRed())),
          (int) (c1.getGreen() + p * (c2.getGreen() - c1.getGreen())),
          (int) (c1.getBlue()  + p * (c2.getBlue()  - c1.getBlue()))
      );
    }
  }

  record ConstColor(Color color) implements ColorSupplier {
    @Override
    public Color getColor(double completion) {
      return color;
    }
  }

  record ScriptCallableColor(Callable callable, Context cx, Scriptable scope)
      implements ColorSupplier
  {

    @Override
    public Color getColor(double completion) {
      Object[] args = new Object[1];
      args[0] = completion;

      Object o = callable.call(cx, scope, scope, args);
      return ParticleSpawn.getColorLiteral(o);
    }
  }

  interface PointTransformer {
    void apply(Vector3d point, int index);
  }
}
