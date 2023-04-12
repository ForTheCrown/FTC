package net.forthecrown.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EntitySelector;
import net.forthecrown.user.User;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UserSelectorResult implements EntitySelector {

  private final UserParseResult result;

  public UserSelectorResult(UserParseResult result) {
    this.result = result;
  }

  @Override
  public Player findPlayer(CommandSource source) throws CommandSyntaxException {
    User user = result.get(source, true);
    return user.getPlayer();
  }

  @Override
  public Entity findEntity(CommandSource source) throws CommandSyntaxException {
    return findPlayer(source);
  }

  @Override
  public List<Player> findPlayers(CommandSource source)
      throws CommandSyntaxException
  {
    return Lists.newArrayList(findPlayer(source));
  }

  @Override
  public List<Entity> findEntities(CommandSource source)
      throws CommandSyntaxException
  {
    return Lists.newArrayList(findPlayer(source));
  }

  @Override
  public boolean isSelfSelector() {
    return result.isSelectorUsed() && result.getSelector().isSelfSelector();
  }

  @Override
  public boolean isWorldLimited() {
    return result.isSelectorUsed() && result.getSelector().isSelfSelector();
  }

  @Override
  public boolean includesEntities() {
    return false;
  }

  @Override
  public int getMaxResults() {
    return result.isSelectorUsed()
        ? result.getSelector().getMaxResults()
        : 1;
  }
}