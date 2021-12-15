package net.forthecrown.economy.market;

import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.utils.transformation.BlockCopyInfo;
import net.forthecrown.utils.transformation.CopyPreProcessor;
import net.forthecrown.utils.transformation.RegionCopyPaste;
import net.forthecrown.utils.math.WorldVec3i;

public class ShopDestroyPreprocessor implements CopyPreProcessor {
    @Override
    public void process(BlockCopyInfo block, RegionCopyPaste paste, boolean async) {
        if(!ShopManager.isShop(block.paste())) return;

        ShopManager manager = Crown.getShopManager();
        SignShop shop = manager.getShop(WorldVec3i.of(block.paste()));
        if(shop == null) return;

        shop.destroy(false);
    }
}
