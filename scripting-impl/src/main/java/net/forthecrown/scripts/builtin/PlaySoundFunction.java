package net.forthecrown.scripts.builtin;

import net.forthecrown.scripts.ScriptUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

public class PlaySoundFunction implements Callable {

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
    if (args.length < 2) {
      throw ScriptRuntime.typeError("Less than 2 parameters (Player and soundId)");
    }

    Audience audience = ScriptUtils.toSource(args, 0);

    Key soundId;
    Object arg2 = Context.jsToJava(args[1], Object.class);

    if (arg2 instanceof CharSequence str) {
      soundId = Key.key(str.toString());
    } else if (arg2 instanceof Key key) {
      soundId = key;
    } else {
      soundId = Key.key(String.valueOf(arg2));
    }

    float volume = 1;
    float pitch = 1;
    Source source = Source.MASTER;

    if (args.length > 2) {
      volume = (float) ScriptRuntime.toNumber(args, 2);
    }
    if (args.length > 3) {
      pitch = (float) ScriptRuntime.toNumber(args, 3);
    }
    if (args.length > 4) {
      String str = ScriptRuntime.toString(args, 4).toLowerCase();
      source = switch (str) {
        case "music" -> Source.MUSIC;
        case "record" -> Source.RECORD;
        case "weather" -> Source.WEATHER;
        case "block" -> Source.BLOCK;
        case "hostile" -> Source.HOSTILE;
        case "neutral" -> Source.NEUTRAL;
        case "player" -> Source.PLAYER;
        case "ambient" -> Source.AMBIENT;
        case "voice" -> Source.VOICE;

        default -> Source.MASTER;
      };
    }

    Sound sound = Sound.sound()
        .type(soundId)
        .volume(volume)
        .pitch(pitch)
        .source(source)
        .build();

    audience.playSound(sound);
    return Context.getUndefinedValue();
  }
}
