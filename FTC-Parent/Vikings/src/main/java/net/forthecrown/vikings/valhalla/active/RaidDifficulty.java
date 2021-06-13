package net.forthecrown.vikings.valhalla.active;

import net.forthecrown.vikings.valhalla.builder.BattleBuilder;
import org.bukkit.attribute.AttributeModifier;

public class RaidDifficulty {

    public static float MAX_MOD = 10f;
    public static float MIN_MOD = 1f;

    public final float enemyAttackMod;
    public final float enemyHealthMod;

    private BattleBuilder builder;

    public RaidDifficulty(BattleBuilder builder) {
        this.builder = builder;

        this.enemyAttackMod = ensureInRange(calcAttack());
        this.enemyHealthMod = ensureInRange(calcHealth());

        this.builder = null;
    }

    public static float ensureInRange(float mod){
        return Math.min(MAX_MOD, Math.max(MIN_MOD, mod));
    }

    private float calcAttack(){

    }

    private float calcHealth(){

    }

    private AttributeModifier makeMod(float mod){
        return new AttributeModifier("vikings_difficulty_mod", mod, AttributeModifier.Operation.ADD_SCALAR);
    }

    public AttributeModifier healthModifier(){
        return makeMod(enemyHealthMod);
    }

    public AttributeModifier attackModifier(){
        return makeMod(enemyAttackMod);
    }

    public static float medium(){
        return (MAX_MOD - MIN_MOD) / 2;
    }

    public static float hard(){
        return (MAX_MOD - MIN_MOD) / 3 * 2;
    }

    public static float easy(){
        return (MAX_MOD - MIN_MOD) / 3 * 1;
    }
}
