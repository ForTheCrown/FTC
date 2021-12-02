package net.forthecrown.economy;

import net.forthecrown.utils.Struct;
import net.forthecrown.utils.math.MathUtil;
import org.apache.commons.lang3.Validate;

public interface PriceModifier {
    void changePrice(PriceModificationContext context);

    class PriceModificationContext implements Struct {
        public final int basePrice;
        public int currentPrice;

        public PriceModificationContext(int basePrice) {
            this.basePrice = basePrice;
            this.currentPrice = basePrice;
        }
    }

    static PriceModifier taxModifier(float taxAmount /* 0 - 1 */) {
        Validate.isTrue(MathUtil.isInRange(taxAmount, 0, 1), "Given taxAmount was not in range of 0 to 1");

        return context -> {
            float current = context.currentPrice;
            float newPrice = current * taxAmount + current;

            context.currentPrice = (int) newPrice;
        };
    }
}
