package net.forthecrown.guilds;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.currency.Currency;
import net.kyori.adventure.text.Component;

public class GuildExpCurrency implements Currency {

  private final GuildManager manager;

  public GuildExpCurrency(GuildManager manager) {
    this.manager = manager;
  }

  @Override
  public Component format(int amount) {
    return UnitFormat.unit(amount, name());
  }

  @Override
  public float getGainMultiplier(UUID playerId) {
    return manager.getExpModifier().getModifier(playerId);
  }

  @Override
  public String name() {
    return "Guild Exp";
  }

  @Override
  public int get(UUID playerId) {
    return getGuild(playerId).map(GuildMember::getTotalExpEarned).orElse(0);
  }

  @Override
  public void set(UUID playerId, int value) {
    getGuild(playerId).ifPresent(guildMember -> guildMember.setTotalExpEarned(value));
  }

  @Override
  public void add(UUID playerId, int value) {
    getGuild(playerId).ifPresent(guildMember -> guildMember.addExpEarned(value));
  }

  @Override
  public void remove(UUID playerId, int value) {
    getGuild(playerId).ifPresent(guildMember -> guildMember.addExpEarned(-value));
  }

  private Optional<GuildMember> getGuild(UUID playerId) {
    Objects.requireNonNull(playerId, "Null playerId");
    User user = Users.get(playerId);
    Guild guild = Guilds.getGuild(user);

    if (guild == null) {
      return Optional.empty();
    }

    return Optional.of(guild.getMember(playerId));
  }
}
