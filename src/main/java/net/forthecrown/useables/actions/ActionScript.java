package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.util.UsablesScripts;
import net.forthecrown.useables.UsableScriptHolder;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionScript extends UsageAction implements UsableScriptHolder {

  public static final UsageType<ActionScript> TYPE
      = UsageType.of(ActionScript.class)
      .setSuggests(Arguments.SCRIPT::listSuggestions);

  @Getter
  private final String scriptName;

  @Getter
  private final String[] args;

  @Getter @Setter
  private String dataString;

  public ActionScript(String script, String... args) {
    super(TYPE);
    this.scriptName = script;
    this.args = args;
  }

  @Override
  public void onUse(Player player, Usable holder) {
    try (var _script = UsablesScripts.getScript(holder, this)) {
      _script.invoke("onUse", Users.get(player));
    }
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(
        String.format("'%s'", scriptName)
    );
  }

  @Override
  public @Nullable Tag save() {
    return UsablesScripts.saveScript(this);
  }

  @UsableConstructor(ConstructType.PARSE)
  public static ActionScript parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    return UsablesScripts.parseScript(reader, ActionScript::new);
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionScript readTag(Tag tag) {
    return UsablesScripts.loadScript(tag, ActionScript::new);
  }
}