package net.forthecrown.core.user;

import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserNameFactory;
import net.forthecrown.user.UserProperty;
import net.forthecrown.user.UserProperty.Builder;
import net.forthecrown.user.UserService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
public class UserServiceImpl implements UserService {

  @Getter
  private final UserLookupImpl lookup;

  public UserServiceImpl() {
    this.lookup = new UserLookupImpl();
  }

  @Override
  public UserNameFactory getNameFactory() {
    return null;
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