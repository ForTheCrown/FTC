package net.forthecrown.command.help;

import com.mojang.datafixers.util.Either;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.CommandSource;

@Getter
@RequiredArgsConstructor
public class CommandHelpEntry extends AbstractHelpEntry {

  private final FtcCommand command;

  @Override
  public String getCategory() {
    return packageNameToCategory(command.getClass().getPackageName());
  }

  @Override
  public CommandDisplayInfo createDisplay() {
    var category = getCategory();

    if (command.getBuiltNode() != null) {
      return CommandDisplayInfo.create(
          Either.right(command.getBuiltNode()),
          getUsages(),
          command.getHelpListName(),
          category
      );
    }

    return CommandDisplayInfo.create(
        Either.left(command.getCommand()),
        getUsages(),
        command.getHelpListName(),
        category
    );
  }

  @Override
  public boolean test(CommandSource source) {
    return command.canUse(source);
  }

  @Override
  public String toString() {
    return "/" + command.getHelpListName();
  }
}