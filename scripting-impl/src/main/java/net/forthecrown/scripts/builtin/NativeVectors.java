package net.forthecrown.scripts.builtin;

import static org.mozilla.javascript.Context.javaToJS;
import static org.mozilla.javascript.Context.jsToJava;

import net.forthecrown.grenadier.CommandSource;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

final class NativeVectors {

  private static final Callable VEC_3 = new Vec3Function();
  private static final Callable VEC_2 = new Vec2Function();
  private static final Callable VEC_3_XZ = new Vec3MixedFunction(true);
  private static final Callable VEC_3_XY = new Vec3MixedFunction(false);
  private static final Callable VEC_2_XZ = new Vec2MixedFunction(true);
  private static final Callable VEC_2_XY = new Vec2MixedFunction(false);
  private static final Callable LERP = new LerpFunction();

  static void init(Scriptable scriptable) {
    ScriptableObject.putConstProperty(scriptable, "vec3",   VEC_3);
    ScriptableObject.putConstProperty(scriptable, "vec2",   VEC_2);
    ScriptableObject.putConstProperty(scriptable, "vec3xz", VEC_3_XZ);
    ScriptableObject.putConstProperty(scriptable, "vec3xy", VEC_3_XY);
    ScriptableObject.putConstProperty(scriptable, "vec2xz", VEC_2_XZ);
    ScriptableObject.putConstProperty(scriptable, "vec2xy", VEC_2_XY);
    ScriptableObject.putConstProperty(scriptable, "lerp",   LERP);
  }

  static class LerpFunction implements Callable {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (args.length < 3) {
        throw ScriptRuntime.typeError("At least 3 parameters required");
      }

      double progress = ScriptRuntime.toNumber(args, 0);

      Object first = Context.jsToJava(args[1], Object.class);
      Object second = Context.jsToJava(args[2], Object.class);

      if (first instanceof Number num1 && second instanceof Number num2) {
        double point1 = num1.doubleValue();
        double point2 = num2.doubleValue();
        return point1 + ((point2 - point1) * progress);
      }

      if (first instanceof Vector2d v1 && second instanceof Vector2d v2) {
        Vector2d dest = new Vector2d();
        v1.lerp(v2, progress, dest);
        return javaToJS(dest, scope);
      }

      if (first instanceof Vector3d v1 && second instanceof Vector3d v2) {
        Vector3d dest = new Vector3d();
        v1.lerp(v2, progress, dest);
        return javaToJS(dest, scope);
      }

      throw ScriptRuntime.typeError("Both v1 and v2 parameters must be of same type");
    }
  }

  static class Vec3MixedFunction implements Callable {
    final boolean inputIsXZ;

    public Vec3MixedFunction(boolean inputIsXZ) {
      this.inputIsXZ = inputIsXZ;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      int len = Math.min(args.length, 2);

      if (len < 1) {
        return javaToJS(new Vector3d(), scope);
      }

      Vector2d v = (Vector2d) jsToJava(args[0], Vector2d.class);
      double x;
      double y;
      double z;

      if (inputIsXZ) {
        x = v.x;
        z = v.y;

        if (args.length > 1) {
          y = ScriptRuntime.toNumber(args, 1);
        } else {
          y = 0;
        }
      } else {
        x = v.x;
        y = v.y;

        if (args.length > 1) {
          z = ScriptRuntime.toNumber(1);
        } else {
          z = 0;
        }
      }

      return javaToJS(new Vector3d(x, y, z), scope);
    }
  }

  static class Vec2MixedFunction implements Callable {
    final boolean inputIsXZ;

    public Vec2MixedFunction(boolean inputIsXZ) {
      this.inputIsXZ = inputIsXZ;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      int len = Math.min(args.length, 2);

      if (len < 1) {
        return javaToJS(new Vector3d(), scope);
      }

      Vector3d v = (Vector3d) jsToJava(args[0], Vector3d.class);
      double x = v.x;
      double y;

      if (inputIsXZ) {
        y = v.z;
      } else {
        y = v.y;
      }

      return javaToJS(new Vector2d(x, y), scope);
    }
  }

  static class Vec2Function implements Callable {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      int len = Math.min(args.length, 2);

      switch (len) {
        case 2 -> {
          double x = ScriptRuntime.toNumber(args, 0);
          double y = ScriptRuntime.toNumber(args, 1);
          return javaToJS(new Vector2d(x, y), scope);
        }

        case 1 -> {
          Object arg = jsToJava(args[0], Object.class);

          if (arg instanceof Vector2d vec2) {
            return javaToJS(new Vector2d(vec2), scope);
          }

          double value = ScriptRuntime.toNumber(arg);
          return javaToJS(new Vector2d(value, value), scope);
        }

        default -> {
          return javaToJS(new Vector2d(), scope);
        }
      }
    }
  }

  static class Vec3Function implements Callable {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      int len = Math.min(args.length, 3);

      switch (len) {
        case 3 -> {
          double x = ScriptRuntime.toNumber(args, 0);
          double y = ScriptRuntime.toNumber(args, 1);
          double z = ScriptRuntime.toNumber(args, 2);
          return javaToJS(new Vector3d(x, y, z), scope);
        }

        case 2 -> {
          double x = ScriptRuntime.toNumber(args, 0);
          double y = ScriptRuntime.toNumber(args, 1);
          return javaToJS(new Vector3d(x, y, 0), scope);
        }

        case 1 -> {
          Object o = jsToJava(args[0], Object.class);

          if (o instanceof CommandSource source) {
            o = source.getLocation();
          } else if (o instanceof Entity entity) {
            o = entity.getLocation();
          } else if (o instanceof Block block) {
            o = block.getLocation();
          } else if (o instanceof BlockState state) {
            o = state.getLocation();
          }

          if (o instanceof Vector3d vec3) {
            return javaToJS(new Vector3d(vec3), scope);
          } else if (o instanceof Location loc) {
            return javaToJS(new Vector3d(loc.x(), loc.y(), loc.z()), scope);
          } else if (o instanceof Vector ve) {
            return javaToJS(new Vector3d(ve.getX(), ve.getY(), ve.getZ()), scope);
          } else {
            double value = ScriptRuntime.toNumber(o);
            return javaToJS(new Vector3d(value, value, value), scope);
          }
        }

        default -> {
          return javaToJS(new Vector3d(), scope);
        }
      }
    }
  }
}
