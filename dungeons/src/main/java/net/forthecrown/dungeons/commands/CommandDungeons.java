package net.forthecrown.dungeons.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import org.bukkit.entity.Player;

@CommandData("file = 'commands/dungeons.gcn'")
public class CommandDungeons {

  @VariableInitializer
  void initVars(Map<String, Object> variables) {
    variables.put("apple", ArgumentTypes.enumType(BossItems.class));

    RegistryArguments<KeyedBoss> argument = new RegistryArguments<>(Bosses.REGISTRY, "DungeonBoss");
    variables.put("boss", argument);
  }

  void giveApple(CommandSource source, @Argument("boss_apple") BossItems apple)
      throws CommandSyntaxException
  {
    Player player = source.asPlayer();

    DungeonUtils.giveOrDropItem(
        player.getInventory(),
        player.getLocation(),
        apple.item()
    );

    source.sendMessage("Gave " + apple.name().toLowerCase() + " apple");
  }

  void normalSpawn(CommandSource source, @Argument("boss") DungeonBoss boss)
      throws CommandSyntaxException
  {
    ensureDead(boss);
    Player player = source.asPlayer();

    if (boss.attemptSpawn(player)) {
      source.sendSuccess(Text.format("Summoning {0}", boss.name()));
    }
  }

  void forceSpawn(CommandSource source, @Argument("boss") DungeonBoss boss)
      throws CommandSyntaxException
  {
    ensureDead(boss);
    boss.spawn();
    source.sendSuccess(Text.format("Summoning {0}", boss.name()));
  }

  void ensureDead(DungeonBoss boss) throws CommandSyntaxException {
    if (!boss.isAlive()) {
      return;
    }

    throw Exceptions.format("Boss {0} has already been summoned", boss.name());
  }

  void killBoss(CommandSource source, @Argument("boss") DungeonBoss boss)
      throws CommandSyntaxException
  {
    if (boss.isAlive()) {
      throw Exceptions.format("Boss '{0}' is alive", boss.name());
    }

    boss.kill(false);
    source.sendSuccess(Text.format("Killing {0}", boss.name()));
  }
}