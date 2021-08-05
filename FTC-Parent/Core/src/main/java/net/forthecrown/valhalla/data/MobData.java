package net.forthecrown.valhalla.data;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.valhalla.RaidDifficulty;
import net.forthecrown.valhalla.RaidGenerationContext;

public class MobData implements RaidData {
    private ObjectList<EntitySpawnData> passive;
    private ObjectList<EntitySpawnData> hostile;
    private ObjectList<EntitySpawnData> special;

    public MobData(ObjectList<EntitySpawnData> passive, ObjectList<EntitySpawnData> hostile, ObjectList<EntitySpawnData> special) {
        this.passive = passive;
        this.hostile = hostile;
        this.special = special;
    }

    public MobData() { }

    @Override
    public void generate(RaidGenerationContext context) {
        spawn(hostile, context);
        spawn(passive, context);

        if(context.getDifficulty().getModifier() >= RaidDifficulty.medium().getModifier()) spawn(special, context);
    }

    private void spawn(ObjectList<EntitySpawnData> dataList, RaidGenerationContext context) {
        if(ListUtils.isNullOrEmpty(dataList)) return;

        dataList.forEach(d -> d.spawn(context));
    }

    public ObjectList<EntitySpawnData> getHostile() {
        return hostile;
    }

    public boolean hasHostile() {
        return !ListUtils.isNullOrEmpty(hostile);
    }

    public void addHostile(EntitySpawnData data) {
        if(hostile == null) hostile = new ObjectArrayList<>();
        hostile.add(data);
    }

    public void removeHostile(int index) {
        if(hostile == null) return;
        hostile.remove(index);
    }

    public ObjectList<EntitySpawnData> getPassive() {
        return passive;
    }

    public boolean hasPassive() {
        return !ListUtils.isNullOrEmpty(passive);
    }

    public void addPassive(EntitySpawnData data) {
        if(passive == null) passive = new ObjectArrayList<>();
        passive.add(data);
    }

    public void removePassive(int index) {
        if(passive == null) return;
        passive.remove(index);
    }

    public ObjectList<EntitySpawnData> getSpecial() {
        return special;
    }

    public boolean hasSpecial() {
        return !ListUtils.isNullOrEmpty(special);
    }

    public void addSpecial(EntitySpawnData data) {
        if(special == null) special = new ObjectArrayList<>();
        special.add(data);
    }

    public void removeSpecial(int index) {
        if(special == null) return;
        special.remove(index);
    }
}
