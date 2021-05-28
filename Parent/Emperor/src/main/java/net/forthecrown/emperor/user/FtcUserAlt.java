package net.forthecrown.emperor.user;

import net.forthecrown.emperor.user.data.SoldMaterialData;
import net.forthecrown.emperor.user.enums.Branch;
import net.forthecrown.emperor.user.enums.Pet;
import net.forthecrown.emperor.user.enums.SellAmount;
import org.bukkit.Material;

import java.util.List;
import java.util.Objects;
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
    public List<Pet> getPets() { return getMain().getPets(); }
    @Override
    public boolean hasPet(Pet pet) { return getMain().hasPet(pet); }
    @Override
    public void setPets(List<Pet> pets) { getMain().setPets(pets); }
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
    public void setKing(boolean king, boolean setPrefix) { getMain().setKing(king, setPrefix); }
    @Override
    public void setKing(boolean king, boolean setPrefix, boolean isFemale) { getMain().setKing(king, setPrefix, isFemale); }
    @Override
    public void setKing(boolean king) { getMain().setKing(king); }
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FtcUserAlt that = (FtcUserAlt) o;
        return mainID.equals(that.mainID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mainID);
    }
}
