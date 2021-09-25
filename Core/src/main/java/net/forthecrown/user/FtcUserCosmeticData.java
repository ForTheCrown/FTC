package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;

import java.util.Set;

public class FtcUserCosmeticData extends AbstractUserAttachment implements CosmeticData {

    public final Set<ArrowEffect> arrowEffects = new ObjectOpenHashSet<>();
    public final Set<DeathEffect> deathEffects = new ObjectOpenHashSet<>();
    public final Set<TravelEffect> travelEffects = new ObjectOpenHashSet<>();

    public ArrowEffect arrow;
    public DeathEffect death;
    public TravelEffect travel;

    FtcUserCosmeticData(FtcUser user){
        super(user);
    }

    @Override
    public DeathEffect getActiveDeath() {
        return death;
    }

    @Override
    public void setActiveDeath(DeathEffect death) {
        this.death = death;
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
    public TravelEffect getActiveTravel() {
        return travel;
    }

    @Override
    public void setActiveTravel(TravelEffect effect) {
        travel = effect;
    }

    @Override
    public Set<DeathEffect> getDeathEffects() {
        return deathEffects;
    }

    @Override
    public boolean hasDeath(DeathEffect effect){
        return deathEffects.contains(effect);
    }

    @Override
    public void addDeath(DeathEffect effect){
        deathEffects.add(effect);
    }

    @Override
    public void removeDeath(DeathEffect effect){
        deathEffects.remove(effect);
    }

    @Override
    public Set<ArrowEffect> getArrowEffects() {
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
    public Set<TravelEffect> getTravelEffects() {
        return travelEffects;
    }

    @Override
    public boolean hasTravel(TravelEffect effect) {
        return travelEffects.contains(effect);
    }

    @Override
    public void addTravel(TravelEffect effect) {
        travelEffects.add(effect);
    }

    @Override
    public void removeTravel(TravelEffect effect) {
        travelEffects.remove(effect);
    }

    @Override
    public void deserialize(JsonElement element) {
        //Clear and nullify before trying to load
        arrowEffects.clear();
        deathEffects.clear();
        travelEffects.clear();

        arrow = null;
        death = null;
        travel = null;

        if(element == null) return;
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        if(json.has("arrow")) arrow = Registries.ARROW_EFFECTS.get(json.getKey("arrow"));
        if(json.has("death")) death = Registries.DEATH_EFFECTS.get(json.getKey("death"));
        if(json.has("travel")) travel = Registries.TRAVEL_EFFECTS.get(json.getKey("travel"));

        if(json.has("arrowEffects")) {
            arrowEffects.addAll(json.getList("arrowEffects", e -> Registries.ARROW_EFFECTS.get(JsonUtils.readKey(e))));
        }

        if(json.has("deathEffects")) {
            deathEffects.addAll(json.getList("deathEffects", e -> Registries.DEATH_EFFECTS.get(JsonUtils.readKey(e))));
        }

        if(json.has("travelEffects")) {
            travelEffects.addAll(json.getList("travelEffects", e -> Registries.TRAVEL_EFFECTS.get(JsonUtils.readKey(e))));
        }
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        //Arrow and death effects are serialized by their keys
        if(hasActiveArrow()) json.add("arrow", arrow);
        if(hasActiveDeath()) json.add("death", death);
        if(hasActiveTravel()) json.add("travel", travel);

        if(!arrowEffects.isEmpty()) json.addList("arrowEffects", arrowEffects);
        if(!deathEffects.isEmpty()) json.addList("deathEffects", deathEffects);
        if(!travelEffects.isEmpty()) json.addList("travelEffects", travelEffects);

        return json.nullIfEmpty();
    }
}
