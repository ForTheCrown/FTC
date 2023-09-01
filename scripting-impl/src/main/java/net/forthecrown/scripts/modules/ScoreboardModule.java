package net.forthecrown.scripts.modules;

import java.util.Optional;
import net.forthecrown.scripts.module.JsModule;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

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
  static final int ID_setCriteria = 12;
  static final int ID_setDisplaySlot = 13;
  static final int ID_getDisplayName = 14;
  static final int ID_getRenderType = 15;
  static final int ID_getCriteria = 16;
  static final int ID_getDisplaySlot = 17;

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
  static final String NAME_setCriteria = "setCriteria";
  static final String NAME_setDisplaySlot = "setDisplaySlot";
  static final String NAME_getDisplayName = "getDisplayName";
  static final String NAME_getRenderType = "getRenderType";
  static final String NAME_getCriteria = "getCriteria";
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
  protected void initPrototypeId(int id) {
    String name = getInstanceIdName(id);
    int arity = switch (id) {
      case ID_objectiveExists -> 1;
      case ID_defineObjective -> 2;
      case ID_deleteObjective -> 1;
      case ID_listObjectives -> 0;
      case ID_getScore -> 2;
      case ID_setScore -> 3;
      case ID_addScore -> 3;
      case ID_removeScore -> 3;
      case ID_deleteScore -> 2;
      case ID_setDisplayName -> 2;
      case ID_setRenderType -> 2;
      case ID_setCriteria -> 2;
      case ID_setDisplaySlot -> 2;
      case ID_getDisplayName -> 1;
      case ID_getRenderType -> 1;
      case ID_getCriteria -> 1;
      case ID_getDisplaySlot -> 1;
      default -> throw new IllegalStateException();
    };

    initPrototypeMethod("Scoreboard", id, name, arity);
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
      case NAME_setCriteria -> ID_setCriteria;
      case NAME_setDisplaySlot -> ID_setDisplaySlot;
      case NAME_getDisplayName -> ID_getDisplayName;
      case NAME_getRenderType -> ID_getRenderType;
      case NAME_getCriteria -> ID_getCriteria;
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
      case ID_setCriteria -> NAME_setCriteria;
      case ID_setDisplaySlot -> NAME_setDisplaySlot;
      case ID_getDisplayName -> NAME_getDisplayName;
      case ID_getRenderType -> NAME_getRenderType;
      case ID_getCriteria -> NAME_getCriteria;
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

        NativeArray arr = new NativeArray(set.size());
        for (Objective objective : set) {
          arr.add(objective.getName());
        }

        yield arr;
      }

      case ID_getScore -> {
        String name = ScriptRuntime.toString(args, 0);
        String playerName = ScriptRuntime.toString(args, 1);

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

      default -> throw f.unknown();
    };
  }

  Optional<Objective> getObjective(String name) {
    return Optional.ofNullable(getScoreboard().getObjective(name));
  }

  Scoreboard getScoreboard() {
    return Bukkit.getScoreboardManager().getMainScoreboard();
  }
}
