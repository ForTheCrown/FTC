package net.forthecrown.vault;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.forthecrown.text.Text;
import net.forthecrown.user.Users;
import net.forthecrown.user.currency.Currency;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.spongepowered.math.GenericMath;

public class FtcEconomy extends AbstractEconomy {

  void registerService(Plugin plugin) {
    Bukkit.getServicesManager().register(Economy.class, this, plugin, ServicePriority.Highest);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getName() {
    return "FTC-Economy";
  }

  @Override
  public boolean hasBankSupport() {
    return false;
  }

  @Override
  public int fractionalDigits() {
    return 0;
  }

  @Override
  public String format(double amount) {
    return Text.NUMBER_FORMAT.format(amount);
  }

  @Override
  public String currencyNamePlural() {
    return "Rhines";
  }

  @Override
  public String currencyNameSingular() {
    return "Rhine";
  }

  private Optional<Balance> getPlayerBalance(String playerName) {
    var service = Users.getService();
    var entry = service.getLookup().query(playerName);

    if (entry == null) {
      return Optional.empty();
    }

    var currencyOpt = service.getCurrencies().get("rhines");

    if (currencyOpt.isEmpty()) {
      return Optional.empty();
    }

    var balance = new Balance(entry.getUniqueId(), currencyOpt.get());
    return Optional.of(balance);
  }

  @Override
  public boolean hasAccount(String playerName) {
    return true;
  }

  @Override
  public boolean hasAccount(String playerName, String worldName) {
    return true;
  }

  @Override
  public double getBalance(String playerName) {
    return getPlayerBalance(playerName).map(Balance::get).orElse(0);
  }

  @Override
  public double getBalance(String playerName, String world) {
    return getPlayerBalance(playerName).map(Balance::get).orElse(0);
  }

  @Override
  public boolean has(String playerName, double amount) {
    return getPlayerBalance(playerName).map(balance -> balance.get() >= amount).orElse(false);
  }

  @Override
  public boolean has(String playerName, String worldName, double amount) {
    return getPlayerBalance(playerName).map(balance -> balance.get() >= amount).orElse(false);
  }

  @Override
  public EconomyResponse withdrawPlayer(String playerName, double amount) {
    return getPlayerBalance(playerName)
        .map(balance -> {
          int reduction = GenericMath.floor(amount);
          int currentBalance = balance.get();

          if (currentBalance < amount) {
            return new EconomyResponse(
                amount, currentBalance, ResponseType.FAILURE,
                "Cannot afford " + format(amount) + " Rhines"
            );
          }

          int nBal = currentBalance - reduction;
          balance.set(nBal);

          return new EconomyResponse(amount, nBal, ResponseType.SUCCESS, "");
        })
        .orElseGet(() -> new EconomyResponse(amount, 0, ResponseType.NOT_IMPLEMENTED, ""));
  }

  @Override
  public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
    return withdrawPlayer(playerName, amount);
  }

  @Override
  public EconomyResponse depositPlayer(String playerName, double amount) {
    return getPlayerBalance(playerName)
        .map(balance -> {
          int addition = GenericMath.floor(amount);
          int nBal = balance.get() + addition;

          balance.set(nBal);

          return new EconomyResponse(amount, nBal, ResponseType.SUCCESS, "");
        })
        .orElseGet(() -> new EconomyResponse(amount, 0, ResponseType.NOT_IMPLEMENTED, ""));
  }

  @Override
  public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
    return depositPlayer(playerName, amount);
  }

  @Override
  public EconomyResponse createBank(String name, String player) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public EconomyResponse deleteBank(String name) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public EconomyResponse bankBalance(String name) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public EconomyResponse bankHas(String name, double amount) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public EconomyResponse bankWithdraw(String name, double amount) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public EconomyResponse bankDeposit(String name, double amount) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public EconomyResponse isBankOwner(String name, String playerName) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public EconomyResponse isBankMember(String name, String playerName) {
    return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "");
  }

  @Override
  public List<String> getBanks() {
    return List.of();
  }

  @Override
  public boolean createPlayerAccount(String playerName) {
    return true;
  }

  @Override
  public boolean createPlayerAccount(String playerName, String worldName) {
    return true;
  }

  record Balance(UUID uuid, Currency currency) {

    int get() {
      return currency.get(uuid);
    }

    void set(int amount) {
      currency.set(uuid, amount);
    }
  }
}
