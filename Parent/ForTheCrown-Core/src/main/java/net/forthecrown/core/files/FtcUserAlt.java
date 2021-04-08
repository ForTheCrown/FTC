package net.forthecrown.core.files;

import net.forthecrown.core.api.*;
import net.forthecrown.core.enums.SellAmount;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FtcUserAlt extends FtcUser implements CrownUserAlt {

    private UUID mainID;
    private CrownUser main;

    public FtcUserAlt(UUID base, UUID main){
        super(base);
        this.mainID = main;
        this.main = UserManager.getUser(main);
        UserManager.LOADED_ALTS.put(base, this);
    }

    @Override
    public UUID getMainUniqueID() {
        if(mainID == null) mainID = UserManager.inst().getMain(getUniqueId());
        return mainID;
    }

    @Override
    public CrownUser getMain() {
        if(main == null) main = UserManager.getUser(getMainUniqueID());
        return main;
    }

    @Override
    protected boolean shouldResetEarnings() { return System.currentTimeMillis() > getMain().getNextResetTime(); }
    @Override
    public Grave getGrave(){ return getMain().getGrave(); }
    @Override
    public boolean getCanSwapBranch() { return getMain().getCanSwapBranch(); }
    @Override
    public void setCanSwapBranch(boolean canSwapBranch, boolean addToCooldown) { getMain().setCanSwapBranch(canSwapBranch, addToCooldown); }
    @Override
    public long getNextAllowedBranchSwap() { return getMain().getNextAllowedBranchSwap(); }
    @Override
    public boolean performBranchSwappingCheck() { return getMain().performBranchSwappingCheck(); }
    @Override
    public List<String> getPets() { return getMain().getPets(); }
    @Override
    public void setPets(List<String> pets) { getMain().setPets(pets); }
    @Override
    public Short getItemPrice(Material item) { return getMain().getItemPrice(item); }
    @Override
    public void setItemPrice(Material item, short price) { getMain().setItemPrice(item, price); }
    @Override
    public Map<Material, Short> getItemPrices() { return getMain().getItemPrices(); }
    @Override
    public void setItemPrices(@NotNull Map<Material, Short> itemPrices) { getMain().setItemPrices(itemPrices); }
    @Override
    public Integer getAmountEarned(Material material) { return getMain().getAmountEarned(material); }
    @Override
    public void setAmountEarned(Material material, Integer amount) { getMain().setAmountEarned(material, amount); }
    @Override
    public Map<Material, Integer> getAmountEarnedMap() { return getMain().getAmountEarnedMap(); }
    @Override
    public void setAmountEarnedMap(@NotNull Map<Material, Integer> amountSold) { getMain().setAmountEarnedMap(amountSold); }
    @Override
    public long getTotalEarnings() { return getMain().getTotalEarnings(); }
    @Override
    public void setTotalEarnings(long amount) { getMain().setTotalEarnings(amount); }
    @Override
    public void addTotalEarnings(long amount) { getMain().addTotalEarnings(amount); }
    @Override
    public long getNextResetTime() { return getMain().getNextResetTime(); }
    @Override
    public void setNextResetTime(long nextResetTime) { getMain().setNextResetTime(nextResetTime); }
    @Override
    public boolean isKing() { return getMain().isKing(); }
    @Override
    public void setKing(boolean king, boolean setPrefix) { getMain().setKing(king, setPrefix); }
    @Override
    public void setKing(boolean king, boolean setPrefix, boolean isFemale) { getMain().setKing(king, setPrefix, isFemale); }
    @Override
    public void setKing(boolean king) { getMain().setKing(king); }
    @Override
    public short configurePriceForItem(Material item) { return getMain().configurePriceForItem(item); }
    @Override
    public boolean allowsRidingPlayers() { return getMain().allowsRidingPlayers(); }
    @Override
    public void setAllowsRidingPlayers(boolean allowsRidingPlayers) { getMain().setAllowsRidingPlayers(allowsRidingPlayers); }
    @Override
    public boolean allowsEmotes() { return getMain().allowsEmotes(); }
    @Override
    public void setAllowsEmotes(boolean allowsEmotes) { getMain().setAllowsEmotes(allowsEmotes); }
    @Override
    public SellAmount getSellAmount() { return getMain().getSellAmount(); }
    @Override
    public void setSellAmount(SellAmount sellAmount) { getMain().setSellAmount(sellAmount); }
    @Override
    public void resetEarnings() { getMain().resetEarnings(); }
    @Override
    public void clearTabPrefix() { getMain().clearTabPrefix(); }
    @Override
    public UserDataContainer getDataContainer() { return getMain().getDataContainer(); }
}
