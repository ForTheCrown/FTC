package net.forthecrown.scripts.modules;

import java.util.Optional;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.scripts.ScriptUtils;
import net.forthecrown.scripts.module.JsModule;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.json.JsonParser;
import org.mozilla.javascript.json.JsonParser.ParseException;

public class ScoreboardModule extends IdScriptableObject {

  static final int ID_objectiveExists = 1;
  static final int ID_defineObjective = 2;
  static final int ID_deleteObjective = 3;
  static final int ID_listObjectives = 4;
  static final int ID_getScore = 5;
  static final int ID_setScore = 6;
  static final int ID_addScore = 7;
  static final int ID_removeScore = 8;
  static final int ID_deleteScore = 9;
  static final int ID_setDisplayName = 10;
  static final int ID_setRenderType = 11;
  static final int ID_setDisplaySlot = 12;
  static final int ID_getDisplayName = 13;
  static final int ID_getRenderType = 14;
  static final int ID_getDisplaySlot = 15;

  static final int MAX_ID = ID_getDisplaySlot;

  static final String NAME_objectiveExists = "objectiveExists";
  static final String NAME_defineObjective = "defineObjective";
  static final String NAME_deleteObjective = "deleteObjective";
  static final String NAME_listObjectives = "listObjectives";
  static final String NAME_getScore = "getScore";
  static final String NAME_setScore = "setScore";
  static final String NAME_addScore = "addScore";
  static final String NAME_removeScore = "removeScore";
  static final String NAME_deleteScore = "deleteScore";
  static final String NAME_setDisplayName = "setDisplayName";
  static final String NAME_setRenderType = "setRenderType";
  static final String NAME_setDisplaySlot = "setDisplaySlot";
  static final String NAME_getDisplayName = "getDisplayName";
  static final String NAME_getRenderType = "getRenderType";
  static final String NAME_getDisplaySlot = "getDisplaySlot";

  public static final JsModule MODULE = scope -> {
    ScoreboardModule module = new ScoreboardModule();
    module.activatePrototypeMap(MAX_ID);
    module.setParentScope(scope);
    return module;
  };

  private ScoreboardModule() {

  }

  @Override
  public String getClassName() {
    return "Scoreboard";
  }

  @Override
  protected Object getInstanceIdValue(int id) {
    return NOT_FOUND;
  }

  @Override
  public Object[] getIds() {
    Object[] ids = new Object[MAX_ID];
    for (int i = 1; i <= MAX_ID; i++) {
      ids[i-1] = getInstanceIdName(i);
    }
    return ids;
  }

  @Override
  protected void initPrototypeId(int id) {
    String name = getInstanceIdName(id);
    int arity = switch (id) {
      case ID_objectiveExists -> 1;
      case ID_defineObjective -> 2;
      case ID_deleteObjective -> 1;
      case ID_listObjectives  -> 0;
      case ID_getScore        -> 2;
      case ID_setScore        -> 3;
      case ID_addScore        -> 3;
      case ID_removeScore     -> 3;
      case ID_deleteScore     -> 2;
      case ID_setDisplayName  -> 2;
      case ID_setRenderType   -> 2;
      case ID_setDisplaySlot  -> 2;
      case ID_getDisplayName  -> 1;
      case ID_getRenderType   -> 1;
      case ID_getDisplaySlot  -> 1;
      default -> throw new IllegalStateException();
    };

    initPrototypeMethod("Scoreboard", id, name, arity);
  }

  @Override
  protected int getMaxInstanceId() {
    return MAX_ID;
  }

  @Override
  protected int findPrototypeId(String name) {
    return findInstanceIdInfo(name);
  }

  @Override
  protected int findInstanceIdInfo(String name) {
    return switch (name) {
      case NAME_objectiveExists -> ID_objectiveExists;
      case NAME_defineObjective -> ID_defineObjective;
      case NAME_deleteObjective -> ID_deleteObjective;
      case NAME_listObjectives -> ID_listObjectives;
      case NAME_getScore -> ID_getScore;
      case NAME_setScore -> ID_setScore;
      case NAME_addScore -> ID_addScore;
      case NAME_removeScore -> ID_removeScore;
      case NAME_deleteScore -> ID_deleteScore;
      case NAME_setDisplayName -> ID_setDisplayName;
      case NAME_setRenderType -> ID_setRenderType;
      case NAME_setDisplaySlot -> ID_setDisplaySlot;
      case NAME_getDisplayName -> ID_getDisplayName;
      case NAME_getRenderType -> ID_getRenderType;
      case NAME_getDisplaySlot -> ID_getDisplaySlot;
      default -> 0;
    };
  }

  @Override
  protected String getInstanceIdName(int id) {
    return switch (id) {
      case ID_objectiveExists -> NAME_objectiveExists;
      case ID_defineObjective -> NAME_defineObjective;
      case ID_deleteObjective -> NAME_deleteObjective;
      case ID_listObjectives -> NAME_listObjectives;
      case ID_getScore -> NAME_getScore;
      case ID_setScore -> NAME_setScore;
      case ID_addScore -> NAME_addScore;
      case ID_removeScore -> NAME_removeScore;
      case ID_deleteScore -> NAME_deleteScore;
      case ID_setDisplayName -> NAME_setDisplayName;
      case ID_setRenderType -> NAME_setRenderType;
      case ID_setDisplaySlot -> NAME_setDisplaySlot;
      case ID_getDisplayName -> NAME_getDisplayName;
      case ID_getRenderType -> NAME_getRenderType;
      case ID_getDisplaySlot -> NAME_getDisplaySlot;
      default -> throw new IllegalArgumentException(String.valueOf(id));
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
    return switch (f.methodId()) {
      case ID_objectiveExists -> {
        String name = ScriptRuntime.toString(args, 0);
        yield getScoreboard().getObjective(name) != null;
      }

      case ID_defineObjective -> {
        String name = ScriptRuntime.toString(args, 0);
        String criteria = ScriptRuntime.toString(args, 1);

        var opt = getObjective(name);

        if (opt.isPresent()) {
          yield false;
        }

        getScoreboard().registerNewObjective(name, criteria);
        yield true;
      }

      case ID_deleteObjective -> {
        String name = ScriptRuntime.toString(args, 0);

        yield getObjective(name)
            .map(objective -> {
              objective.unregister();
              return true;
            })
            .orElse(false);
      }

      case ID_listObjectives -> {
        var scoreboard = getScoreboard();
        var set = scoreboard.getObjectives();

        Scriptable arr = cx.newArray(scope, set.size());

        int i = 0;
        for (Objective objective : set) {
          ScriptableObject.putProperty(arr, i, objective.getName());
          i++;
        }

        yield arr;
      }

      case ID_getScore -> {
        String name = ScriptRuntime.toString(args, 0);
        String playerName = getEntryName(args, 1);

        yield getObjective(name)
            .flatMap(objective -> {
              Score score = objective.getScore(playerName);

              if (!score.isScoreSet()) {
                return Optional.empty();
              }

              return Optional.of((Object) score.getScore());
            })
            .orElse(Context.getUndefinedValue());
      }

      case ID_setScore    -> modifyScore(args,  0);
      case ID_addScore    -> modifyScore(args,  1);
      case ID_removeScore -> modifyScore(args, -1);
      case ID_deleteScore -> modifyScore(args, -2);

      case ID_setDisplayName -> {
        var name = ScriptRuntime.toString(args, 0);
        var objOpt = getObjective(name);

        if (objOpt.isEmpty()) {
          yield false;
        }

        if (args.length != 2) {
          throw ScriptRuntime.typeError(
              "2 params required, objectiveName: string and displayName: text"
          );
        }

        Component displayName = ScriptUtils.toText(cx, scope, args, 1);

        if (displayName == null) {
          throw ScriptRuntime.typeError("displayName cannot be null");
        }

        objOpt.get().displayName(displayName);
        yield true;
      }

      case ID_setRenderType -> {
        var name = ScriptRuntime.toString(args, 0);
        var objOpt = getObjective(name);

        if (objOpt.isEmpty()) {
          yield false;
        }

        var obj = objOpt.get();
        var typeStr = ScriptRuntime.toString(args, 1);

        RenderType type = switch (typeStr.toLowerCase()) {
          case "integer", "integers", "int", "number", "numbers", "num", "nums"
              -> RenderType.INTEGER;

          case "heart", "hearts"
              -> RenderType.HEARTS;

          default -> throw ScriptRuntime.typeError("Invalid RenderType: '" + typeStr + "'");
        };

        obj.setRenderType(type);
        yield true;
      }

      case ID_setDisplaySlot -> {
        var name = ScriptRuntime.toString(args, 0);
        var slotStr = ScriptRuntime.toString(args, 1);

        var objOpt = getObjective(name);

        if (objOpt.isEmpty()) {
          yield false;
        }

        Objective obj = objOpt.get();

        if (slotStr.equals("null") || slotStr.equals("undefined")) {
          obj.setDisplaySlot(null);
          yield true;
        }

        DisplaySlot slot = DisplaySlot.NAMES.value(slotStr);

        if (slot == null) {
          throw ScriptRuntime.typeError("Invalid slot");
        }

        obj.setDisplaySlot(slot);
        yield true;
      }

      case ID_getDisplayName -> {
        var optional = toObjective(args).map(Objective::displayName);

        if (optional.isEmpty()) {
          yield null;
        }

        var name = optional.get();
        var json = GsonComponentSerializer.gson().serialize(name);

        try {
          yield new JsonParser(cx, scope).parseValue(json);
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }
      }

      case ID_getRenderType -> {
        yield toObjective(args)
            .map(Objective::getRenderType)
            .map(renderType -> renderType.name().toLowerCase())
            .orElse(null);
      }

      case ID_getDisplaySlot -> {
        yield toObjective(args)
            .map(Objective::getDisplaySlot)
            .map(DisplaySlot::getId)
            .orElse(null);
      }

      default -> throw f.unknown();
    };
  }

  String getEntryName(Object[] args, int index) {
    if (args.length <= index) {
      return "undefined";
    }

    Object arg = Context.jsToJava(args[index], Object.class);

    if (arg instanceof CommandSender entity) {
      return entity.getName();
    }

    if (arg instanceof User user) {
      return user.getName();
    }

    if (arg instanceof CommandSource source) {
      return source.asBukkit().getName();
    }

    return String.valueOf(arg);
  }

  boolean modifyScore(Object[] args, int action) {
    String name = ScriptRuntime.toString(args, 0);
    String playerName = getEntryName(args, 1);
    int scoreValue;

    if (action != -2) {
      scoreValue = ScriptRuntime.toInt32(args, 2);
    } else {
      scoreValue = 0;
    }

    return getObjective(name)
        .map(objective -> {
          Score score = objective.getScore(playerName);

          switch (action) {
            case  1 -> score.setScore(score.getScore() + scoreValue);
            case -1 -> score.setScore(score.getScore() - scoreValue);
            case -2 -> score.resetScore();
            default -> score.setScore(scoreValue);
          }

          return true;
        })
        .orElse(false);
  }

  Optional<Objective> toObjective(Object[] args) {
    String str = ScriptRuntime.toString(args, 0);
    return getObjective(str);
  }

  Optional<Objective> getObjective(String name) {
    return Optional.ofNullable(getScoreboard().getObjective(name));
  }

  Scoreboard getScoreboard() {
    return Bukkit.getScoreboardManager().getMainScoreboard();
  }
}
