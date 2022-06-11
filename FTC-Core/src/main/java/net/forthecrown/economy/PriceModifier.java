package net.forthecrown.economy;

import net.forthecrown.utils.Struct;
import net.forthecrown.utils.math.MathUtil;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
public interface PriceModifier {
    void changePrice(PriceModificationContext context);

    default int modify(int initial) {
        PriceModificationContext context = new PriceModificationContext(initial);
        changePrice(context);
        return context.currentPrice;
    }

    class PriceModificationContext implements Struct {
        public final int basePrice;
        public int currentPrice;

        public PriceModificationContext(int basePrice) {
            this.basePrice = basePrice;
            this.currentPrice = basePrice;
        }
    }

    static PriceModifier taxModifier(float taxAmount /* 0 - 1 */) {
        Validate.isTrue(MathUtil.inRange(taxAmount, 0, 1), "Given taxAmount was not in range of 0 to 1");

        return new PriceModifier() {
            @Override
            public void changePrice(PriceModificationContext context) {
                context.currentPrice = modify(context.currentPrice);
            }

            @Override
            public int modify(int initial) {
                float newPrice = ((float) initial) * taxAmount + initial;

                return  (int) newPrice;
            }
        };
    }

    static int modify(int initial, PriceModifier... modifiers) {
        return modify(initial, Arrays.asList(modifiers));
    }

    static int modify(int initial, Collection<PriceModifier> modifiers) {
        PriceModificationContext context = new PriceModificationContext(initial);

        for (PriceModifier m: modifiers) {
            m.changePrice(context);
        }

        return context.currentPrice;
    }
}