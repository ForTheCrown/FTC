package net.forthecrown.core.api;

import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CrownUserAlt extends CrownUser{
    UUID getMainUniqueID();
    CrownUser getMain();

    @Override
    default Grave getGrave(){
        return getMain().getGrave();
    }

    @Override
    default Set<Rank> getAvailableRanks() {
        return getMain().getAvailableRanks();
    }

    @Override
    default void setAvailableRanks(Set<Rank> ranks) {
        getMain().setAvailableRanks(ranks);
    }

    @Override
    default boolean hasRank(Rank rank) {
        return getMain().hasRank(rank);
    }

    @Override
    default void addRank(Rank rank) {
        getMain().addRank(rank);
    }

    @Override
    default void removeRank(Rank rank) {
        getMain().removeRank(rank);
    }

    @Override
    default Rank getRank() {
        return getMain().getRank();
    }

    @Override
    default void setRank(Rank rank) {
        setRank(rank, true);
    }

    @Override
    default void setRank(Rank rank, boolean setPrefix) {
        getMain().setRank(rank, setPrefix);
    }

    @Override
    default boolean getCanSwapBranch() {
        return getMain().getCanSwapBranch();
    }

    @Override
    default void setCanSwapBranch(boolean canSwapBranch, boolean addToCooldown) {
        getMain().setCanSwapBranch(canSwapBranch, addToCooldown);
    }

    @Override
    default long getNextAllowedBranchSwap() {
        return getMain().getNextAllowedBranchSwap();
    }

    @Override
    default boolean performBranchSwappingCheck() {
        return getMain().performBranchSwappingCheck();
    }

    @Override
    default List<String> getPets() {
        return getMain().getPets();
    }

    @Override
    default void setPets(List<String> pets) {
        getMain().setPets(pets);
    }

    @Override
    default Particle getArrowParticle() {
        return getMain().getArrowParticle();
    }

    @Override
    default void setArrowParticle(Particle particleArrowActive) {
        getMain().setArrowParticle(particleArrowActive);
    }

    @Override
    default List<Particle> getParticleArrowAvailable() {
        return getMain().getParticleArrowAvailable();
    }

    @Override
    default void setParticleArrowAvailable(List<Particle> particleArrowAvailable) {
        getMain().setParticleArrowAvailable(particleArrowAvailable);
    }

    @Override
    default String getDeathParticle() {
        return getMain().getDeathParticle();
    }

    @Override
    default void setDeathParticle(String particleDeathActive) {
        getMain().setDeathParticle(particleDeathActive);
    }

    @Override
    default List<String> getParticleDeathAvailable() {
        return getMain().getParticleDeathAvailable();
    }

    @Override
    default void setParticleDeathAvailable(List<String> particleDeathAvailable) {
        getMain().setParticleDeathAvailable(particleDeathAvailable);
    }

    @Override
    default int getGems() {
        return getMain().getGems();
    }

    @Override
    default void setGems(int gems) {
        getMain().setGems(gems);
    }

    @Override
    default void addGems(int gems) {
        getMain().addGems(gems);
    }

    @Override
    default Short getItemPrice(Material item) {
        return getMain().getItemPrice(item);
    }

    @Override
    default void setItemPrice(Material item, short price) {
        getMain().setItemPrice(item, price);
    }

    @Override
    default Map<Material, Short> getItemPrices() {
        return getMain().getItemPrices();
    }

    @Override
    default void setItemPrices(@NotNull Map<Material, Short> itemPrices) {
        getMain().setItemPrices(itemPrices);
    }

    @Override
    default Integer getAmountEarned(Material material) {
        return getMain().getAmountEarned(material);
    }

    @Override
    default void setAmountEarned(Material material, Integer amount) {
        getMain().setAmountEarned(material, amount);
    }

    @Override
    default Map<Material, Integer> getAmountEarnedMap() {
        return getMain().getAmountEarnedMap();
    }

    @Override
    default void setAmountEarnedMap(@NotNull Map<Material, Integer> amountSold) {
        getMain().setAmountEarnedMap(amountSold);
    }

    @Override
    default boolean isBaron() {
        return getMain().isBaron();
    }

    @Override
    default void setBaron(boolean baron) {
        getMain().setBaron(baron);
    }

    @Override
    default long getTotalEarnings() {
        return getMain().getTotalEarnings();
    }

    @Override
    default void setTotalEarnings(long amount) {
        getMain().setTotalEarnings(amount);
    }

    @Override
    default void addTotalEarnings(long amount) {
        getMain().addTotalEarnings(amount);
    }

    @Override
    default long getNextResetTime() {
        return getMain().getNextResetTime();
    }

    @Override
    default void setNextResetTime(long nextResetTime) {
        getMain().setNextResetTime(nextResetTime);
    }

    @Override
    default Branch getBranch() {
        return getMain().getBranch();
    }

    @Override
    default void setBranch(Branch branch) {
        getMain().setBranch(branch);
    }

    @Override
    default void setTabPrefix(String s) {
        getMain().setTabPrefix(s);
    }

    @Override
    default boolean isKing() {
        return getMain().isKing();
    }

    @Override
    default void setKing(boolean king, boolean setPrefix) {
        getMain().setKing(king, setPrefix);
    }

    @Override
    default void setKing(boolean king, boolean setPrefix, boolean isFemale) {
        getMain().setKing(king, setPrefix, isFemale);
    }

    @Override
    default void setKing(boolean king) {
        getMain().setKing(king);
    }

    @Override
    default boolean isProfilePublic() {
        return getMain().isProfilePublic();
    }

    @Override
    default void setProfilePublic(boolean publicProfile) {
        getMain().setProfilePublic(publicProfile);
    }

    @Override
    default short configurePriceForItem(Material item) {
        return getMain().configurePriceForItem(item);
    }

    @Override
    default void addRank(Rank rank, boolean givePermission) {
        getMain().addRank(rank, givePermission);
    }

    @Override
    default void removeRank(Rank rank, boolean removePermission) {
        getMain().removeRank(rank, removePermission);
    }

    @Override
    default boolean allowsRidingPlayers() {
        return getMain().allowsRidingPlayers();
    }

    @Override
    default void setAllowsRidingPlayers(boolean allowsRidingPlayers) {
        getMain().setAllowsRidingPlayers(allowsRidingPlayers);
    }

    @Override
    default boolean allowsEmotes() {
        return getMain().allowsEmotes();
    }

    @Override
    default void setAllowsEmotes(boolean allowsEmotes) {
        getMain().setAllowsEmotes(allowsEmotes);
    }

    @Override
    default SellAmount getSellAmount() {
        return getMain().getSellAmount();
    }

    @Override
    default void setSellAmount(SellAmount sellAmount) { getMain().setSellAmount(sellAmount); }

    @Override
    default void resetEarnings() { getMain().resetEarnings(); }

    @Override
    default void clearTabPrefix() { getMain().clearTabPrefix(); }

    @Override
    default UserDataContainer getDataContainer() {
        return getMain().getDataContainer();
    }
}
