package net.forthecrown.core.api;

import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.MessageCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CrownUser extends MessageCommandSender {
    int configurePriceForItem(Material item);

    UUID getBase();
    Player getPlayer();
    OfflinePlayer getOfflinePlayer();

    Set<Rank> getAvailableRanks();
    void setAvailableRanks(Set<Rank> ranks);

    boolean hasRank(Rank rank);
    void addRank(Rank rank);
    void removeRank(Rank rank);
    Rank getRank();
    void setRank(Rank rank);
    void setRank(Rank rank, boolean setPrefix);

    boolean getCanSwapBranch();
    void setCanSwapBranch(boolean canSwapBranch);

    List<String> getPets();
    void setPets(List<String> pets);

    Particle getArrowParticle();
    void setArrowParticle(Particle particleArrowActive);

    List<Particle> getParticleArrowAvailable();
    void setParticleArrowAvailable(List<Particle> particleArrowAvailable);

    String getDeathParticle();
    void setDeathParticle(String particleDeathActive);

    List<String> getParticleDeathAvailable();
    void setParticleDeathAvailable(List<String> particleDeathAvailable);

    boolean allowsRidingPlayers();
    void setAllowsRidingPlayers(boolean allowsRidingPlayers);

    int getGems();
    void setGems(int gems);
    void addGems(int gems);

    boolean allowsEmotes();
    void setAllowsEmotes(boolean allowsEmotes);

    Integer getItemPrice(Material item);
    void setItemPrice(Material item, int price);

    Map<Material, Integer> getItemPrices();
    void setItemPrices(@Nonnull Map<Material, Integer> itemPrices);

    Integer getAmountEarned(Material material);
    void setAmountEarned(Material material, Integer amount);

    Map<Material, Integer> getAmountEarnedMap();
    void setAmountEarnedMap(@Nonnull Map<Material, Integer> amountSold);

    boolean isBaron();
    void setBaron(boolean baron);

    SellAmount getSellAmount();
    void setSellAmount(SellAmount sellAmount);

    long getTotalEarnings();
    void setTotalEarnings(long amount);
    void addTotalEarnings(long amount);

    long getNextResetTime();
    void setNextResetTime(long nextResetTime);

    void resetEarnings();

    String getName();

    Branch getBranch();
    void setBranch(Branch branch);

    void setTabPrefix(String s);

    void clearTabPrefix();

    boolean isOnline();
}
