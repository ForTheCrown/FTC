package net.forthecrown.inventory.weapon.ability;

import com.google.gson.JsonElement;
import java.util.Optional;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.source.Source;
import org.spongepowered.math.GenericMath;

@FunctionalInterface
public interface UseLimit {
  int get(User user);

  static UseLimit fixed(int v) {
    return user -> v;
  }

  static UseLimit script(Source source) {
    Script script = Scripts.newScript(source);
    script.compile();

    return user -> {
      script.put("user", user);
      UserTitles titles = user.getComponent(UserTitles.class);
      int tier = GenericMath.clamp(titles.getTier().ordinal() - 1, 0, 3);

      script.put("tier", tier);
      var res = script.evaluate();
      script.remove("tier");
      script.remove("user");

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
      return script(Scripts.scriptFileSource(prim.getAsString()));
    }

    throw new IllegalArgumentException("Invalid useLimit JSON: " + element);
  }
}