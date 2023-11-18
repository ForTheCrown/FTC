package net.forthecrown.core;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.forthecrown.Worlds;
import net.forthecrown.text.Text;
import net.forthecrown.user.UserService;
import net.forthecrown.utils.Locations;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@Accessors(fluent = true)
@ConfigSerializable
public class CoreConfig {

  private Duration autosaveInterval = Duration.ofMinutes(30);
  private Duration tpCooldown       = Duration.ofSeconds(3);
  private Duration tpaExpireTime    = Duration.ofMinutes(3);

  private int maxNickLength = 16;

  private String[] illegalWorlds = { "world_void", "world_test" };

  private LocalTime dayUpdateTime = LocalTime.of(0, 0, 1);

  private boolean placeholdersDisabled = false;

  private boolean allowHighLevelEnchantments = true;

  private AltJoinPrevention preventAltJoining = AltJoinPrevention.IF_OTHER_ONLINE;

  private boolean mobHealthBarsEnabled = true;
  private boolean damageNumbersEnabled = true;

  private boolean rightClickDeposits = true;

  private int hoppersInOneChunk = 128;

  private double durabilityWarningThreshold = 0.1d;

  private Location firstTimeSpawn;

  public Location firstTimeSpawn() {
    if (firstTimeSpawn == null) {
      return new Location(Worlds.overworld(), 267.5, 77, 267.5, -180, 0);
    }
    return Locations.clone(firstTimeSpawn);
  }

  public enum AltJoinPrevention {
    ALWAYS {
      @Override
      public Optional<Component> mayJoin(UUID playerId, UserService service) {
        if (service.isAltAccount(playerId)) {
          return Optional.of(
              Component.text("Alt accounts may not join the server, play on your main")
          );
        }

        return Optional.empty();
      }
    },

    NEVER {
      @Override
      public Optional<Component> mayJoin(UUID playerId, UserService service) {
        return Optional.empty();
      }
    },

    IF_OTHER_ONLINE {
      @Override
      public Optional<Component> mayJoin(UUID playerId, UserService service) {
        var alreadyOnlineOpt = service.getOtherAccounts(playerId)
            .stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .findAny();

        return alreadyOnlineOpt.map(player -> Text.format(
            "Your other account ({0, user}) is already online",
            player
        ));

      }
    };

    public abstract Optional<Component> mayJoin(UUID playerId, UserService service);
  }
}
