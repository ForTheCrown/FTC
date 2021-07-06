package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.AbstractDeathEffect;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.CrownUtils;

import java.util.ArrayList;
import java.util.List;

public class FtcUserCosmeticData implements CosmeticData, JsonSerializable, JsonDeserializable {

    public List<ArrowEffect> arrowEffects = new ArrayList<>();
    public List<AbstractDeathEffect> deathEffects = new ArrayList<>();

    public ArrowEffect arrow;
    public AbstractDeathEffect death;

    private final FtcUser user;

    FtcUserCosmeticData(FtcUser user){
        this.user = user;
    }

    @Override
    public CrownUser getUser() {
        return user;
    }

    @Override
    public boolean hasActiveDeath(){
        return death != null;
    }

    @Override
    public AbstractDeathEffect getActiveDeath() {
        return death;
    }

    @Override
    public void setActiveDeath(AbstractDeathEffect death) {
        this.death = death;
    }

    @Override
    public boolean hasActiveArrow(){
        return arrow != null;
    }

    @Override
    public ArrowEffect getActiveArrow() {
        return arrow;
    }

    @Override
    public void setActiveArrow(ArrowEffect arrow) {
        this.arrow = arrow;
    }

    @Override
    public List<AbstractDeathEffect> getDeathEffects() {
        return deathEffects;
    }

    @Override
    public boolean hasDeath(AbstractDeathEffect effect){
        return deathEffects.contains(effect);
    }

    @Override
    public void addDeath(AbstractDeathEffect effect){
        deathEffects.add(effect);
    }

    @Override
    public void removeDeath(AbstractDeathEffect effect){
        deathEffects.remove(effect);
    }

    @Override
    public List<ArrowEffect> getArrowEffects() {
        return arrowEffects;
    }

    @Override
    public boolean hasArrow(ArrowEffect effect){
        return arrowEffects.contains(effect);
    }

    @Override
    public void addArrow(ArrowEffect effect){
        arrowEffects.add(effect);
    }

    @Override
    public void removeArrow(ArrowEffect effect){
        arrowEffects.remove(effect);
    }

    @Override
    public void deserialize(JsonElement element) {
        //Clear and nullify before trying to load
        arrowEffects.clear();
        deathEffects.clear();

        arrow = null;
        death = null;

        if(element == null) return;
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        if(json.has("arrow")){
            arrow = Registries.ARROW_EFFECTS.get(json.getKey("arrow"));
        }

        if(json.has("death")){
            death = Registries.DEATH_EFFECTS.get(json.getKey("death"));
        }

        if(json.has("arrowEffects")){
            arrowEffects.addAll(json.getList("arrowEffects", e -> Registries.ARROW_EFFECTS.get(CrownUtils.parseKey(e.getAsString()))));
        }

        if(json.has("deathEffects")){
            deathEffects.addAll(json.getList("deathEffects", e -> Registries.DEATH_EFFECTS.get(CrownUtils.parseKey(e.getAsString()))));
        }
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        //Arrow and death effects are serialized by their keys
        if(hasActiveArrow()) json.add("arrow", arrow);
        if(hasActiveDeath()) json.add("death", death);

        if(!arrowEffects.isEmpty()) json.addList("arrowEffects", arrowEffects);
        if(!deathEffects.isEmpty()) json.addList("deathEffects", deathEffects);

        return json.nullIfEmpty();
    }
}
