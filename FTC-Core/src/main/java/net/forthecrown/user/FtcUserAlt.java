package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.core.Crown;
import net.forthecrown.user.data.*;
import net.forthecrown.user.manager.FtcUserManager;
import net.forthecrown.user.manager.UserManager;
import org.bukkit.Material;

import java.util.Objects;
import java.util.UUID;

public class FtcUserAlt extends FtcUser implements CrownUserAlt {

    private UUID mainID;
    private CrownUser main;

    public FtcUserAlt(UUID base, UUID main){
        super(base);
        this.mainID = main;
        this.main = UserManager.getUser(main);
        FtcUserManager.LOADED_ALTS.put(base, this);
    }

    @Override
    public void unload() {
        super.unload();
        FtcUserManager.LOADED_ALTS.remove(getUniqueId());
    }

    @Override
    public UUID getMainUniqueID() {
        return mainID == null ? mainID = Crown.getUserManager().getMain(getUniqueId()) : mainID;
    }

    @Override
    public CrownUser getMain() {
        return main == null ? main = UserManager.getUser(getMainUniqueID()) : main;
    }

    @Override
    protected boolean shouldResetEarnings() { return System.currentTimeMillis() > getMain().getNextResetTime(); }
    @Override
    public boolean hasPet(Pet pet) { return getMain().hasPet(pet); }
    @Override
    public void addPet(Pet pet) { getMain().addPet(pet); }
    @Override
    public void removePet(Pet pet) { getMain().removePet(pet); }
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
    public SellAmount getSellAmount() { return getMain().getSellAmount(); }
    @Override
    public void setSellAmount(SellAmount sellAmount) { getMain().setSellAmount(sellAmount); }
    @Override
    public void resetEarnings() { getMain().resetEarnings(); }
    @Override
    public UserDataContainer getDataContainer() { return getMain().getDataContainer(); }
    @Override
    public SoldMaterialData getMatData(Material material) { return getMain().getMatData(material); }
    @Override
    public void setMatData(SoldMaterialData data) { getMain().setMatData(data); }
    @Override
    public CosmeticData getCosmeticData() { return getMain().getCosmeticData(); }
    @Override
    public UserInteractions getInteractions() { return getMain().getInteractions(); }
    @Override
    public RankTitle getTitle() { return getMain().getTitle(); }
    @Override
    public void setTitle(RankTitle title) { getMain().setTitle(title); }
    @Override
    public ObjectSet<RankTitle> getAvailableTitles() { return getMain().getAvailableTitles();}
    @Override
    public void addTitle(RankTitle title, boolean givePermissions, boolean setTierIfHigher) { getMain().addTitle(title, givePermissions, setTierIfHigher); }
    @Override
    public void removeTitle(RankTitle title, boolean removePermission) { getMain().removeTitle(title, removePermission); }
    @Override
    public RankTier getRankTier() { return getMain().getRankTier(); }
    @Override
    public void setRankTier(RankTier tier, boolean perms) { getMain().setRankTier(tier, perms); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null) return false;
        if(!(o instanceof FtcUser)) return false;

        return getUniqueId().equals(((FtcUser) o).getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mainID);
    }
}
