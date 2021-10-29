package net.forthecrown.user;

import net.forthecrown.user.data.SoldMaterialData;
import net.forthecrown.user.data.Faction;
import net.forthecrown.user.data.Pet;
import net.forthecrown.user.data.SellAmount;
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
    public UUID getMainUniqueID() {
        return mainID == null ? mainID = UserManager.inst().getMain(getUniqueId()) : mainID;
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
    public void setFaction(Faction faction) { getMain().setFaction(faction); }
    @Override
    public Faction getFaction() { return getMain().getFaction(); }
    @Override
    public SoldMaterialData getMatData(Material material) { return getMain().getMatData(material); }
    @Override
    public void setMatData(SoldMaterialData data) { getMain().setMatData(data); }
    @Override
    public CosmeticData getCosmeticData() { return getMain().getCosmeticData(); }
    @Override
    public UserInteractions getInteractions() { return getMain().getInteractions(); }

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
