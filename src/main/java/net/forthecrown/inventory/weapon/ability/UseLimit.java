package net.forthecrown.inventory.weapon.ability;

import com.google.gson.JsonElement;
import java.util.Optional;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.user.User;
import org.spongepowered.math.GenericMath;

@FunctionalInterface
public interface UseLimit {
  int get(User user);

  static UseLimit fixed(int v) {
    return user -> v;
  }

  static UseLimit script(ScriptSource source) {
    Script script = Script.of(source).compile();
    return user -> {
      script.put("user", user);
      int tier = GenericMath.clamp(
          user.getTitles().getTier().ordinal() - 1,
          0, 3
      );

      script.put("tier", tier);
      var res = script.eval();
      script.getMirror().remove("tier");
      script.getMirror().remove("user");

      return res.result().flatMap(o -> {
        if (o instanceof Number number) {
          return Optional.of(number.intValue());
        }

        if (o instanceof String s) {
          return Optional.of(Integer.valueOf(s));
        }

        return Optional.empty();
      }).orElseThrow();
    };
  }

  static UseLimit load(JsonElement element) {
    var prim = element.getAsJsonPrimitive();

    if (prim.isNumber()) {
      return fixed(prim.getAsInt());
    } else if (prim.isString()) {
      return script(ScriptSource.of(prim.getAsString()));
    }

    throw new IllegalArgumentException("Invalid useLimit JSON: " + element);
  }
}