package net.forthecrown.guilds.multiplier;

import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

public enum MultiplierType {
  GUILD(
      "&7Multiplier that only applies to your guild.",
      "&7Can be obtained by winning events or something similar."
  ) {
    @Override
    public void forEachAffected(DonatorMultiplier multiplier,
                                Consumer<User> consumer
    ) {
      User user = Users.get(multiplier.getDonator());

      var guild = Guilds.getGuild(user);
      assert guild != null;

      guild.getOnlineMembers().forEach(consumer);
    }

    @Override
    public boolean appliesTo(DonatorMultiplier multiplier, UUID uuid) {
      User user = Users.get(multiplier.getDonator());

      var guild = Guilds.getGuild(user);
      assert guild != null;

      return guild.isMember(uuid);
    }
  },

  GLOBAL(
      "&7Multiplier active for all players at once.",
      "&7Can be obtained in the webstore."
  ) {
    @Override
    public void forEachAffected(DonatorMultiplier multiplier,
                                Consumer<User> consumer
    ) {
      Users.getOnline().forEach(consumer);
    }

    @Override
    public boolean appliesTo(DonatorMultiplier multiplier, UUID uuid) {
      return true;
    }
  };

  @Getter
  private final String[] description;

  MultiplierType(String... description) {
    this.description = description;
  }

  public String getDisplayName() {
    return Text.prettyEnumName(this);
  }

  public abstract void forEachAffected(
      DonatorMultiplier multiplier,
      Consumer<User> consumer
  );

  public abstract boolean appliesTo(DonatorMultiplier multiplier, UUID uuid);
}