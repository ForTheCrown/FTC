package net.forthecrown.user;

import net.forthecrown.user.data.SoldMaterialData;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.user.enums.Pet;
import net.forthecrown.user.enums.SellAmount;
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
    public void setBranch(Branch branch) { getMain().setBranch(branch); }
    @Override
    public Branch getBranch() { return getMain().getBranch(); }
    @Override
    public SoldMaterialData getMatData(Material material) { return getMain().getMatData(material); }
    @Override
    public void setMatData(SoldMaterialData data) { getMain().setMatData(data); }
    @Override
    public CosmeticData getCosmeticData() { return getMain().getCosmeticData(); }

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
