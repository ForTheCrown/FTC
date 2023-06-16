package net.forthecrown.core.user;

import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserProperty;
import net.forthecrown.user.UserProperty.Builder;
import net.forthecrown.user.UserService;
import net.forthecrown.utils.ScoreIntMap;
import net.forthecrown.utils.ScoreIntMap.KeyValidator;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
@Getter
public class UserServiceImpl implements UserService {

  private final UserDataStorage storage;

  private final UserLookupImpl lookup;
  private final NameFactoryImpl nameFactory;

  private final ScoreIntMap<UUID> balances;
  private final ScoreIntMap<UUID> gems;
  private final ScoreIntMap<UUID> playtime;
  private final ScoreIntMap<UUID> votes;

  public UserServiceImpl() {
    Path dir = PathUtil.pluginPath();
    this.storage = new UserDataStorage(dir);

    this.lookup = new UserLookupImpl();
    this.nameFactory = new NameFactoryImpl();

    this.balances = new ScoreIntMap<>();
    this.gems     = new ScoreIntMap<>();
    this.playtime = new ScoreIntMap<>();
    this.votes    = new ScoreIntMap<>();

    this.balances.setValidator(KeyValidator.IS_PLAYER);
    this.gems.setValidator(KeyValidator.IS_PLAYER);
    this.playtime.setValidator(KeyValidator.IS_PLAYER);
    this.votes.setValidator(KeyValidator.IS_PLAYER);
  }

  @Override
  public Registry<UserProperty<?>> getUserProperties() {
    return null;
  }

  @Override
  public Builder<Boolean> createBooleanProperty() {
    return null;
  }

  @Override
  public Builder<Component> createTextProperty() {
    return null;
  }

  @Override
  public <E extends Enum<E>> Builder<E> createEnumProperty(Class<E> type) {
    return null;
  }

  @Override
  public void registerComponentType(Class<? extends UserComponent> componentType) {

  }

  @Override
  public @NotNull User getUser(@NotNull UserLookup.LookupEntry entry) {
    return null;
  }

  @Override
  public @Nullable UUID getMainAccount(@NotNull UUID altPlayerId) {
    return null;
  }

  @Override
  public boolean isAltAccount(@NotNull UUID playerId) {
    return false;
  }

  @Override
  public Collection<UUID> getOtherAccounts(@NotNull UUID playerId) {
    return null;
  }

  @Override
  public boolean isAltForAny(@NotNull UUID playerId, @NotNull Collection<Player> players) {
    return false;
  }

  @Override
  public void setAltAccount(@NotNull UUID altPlayerId, @NotNull UUID mainPlayerId) {

  }

  @Override
  public void removeAltAccount(@NotNull UUID altPlayerId) {

  }
}