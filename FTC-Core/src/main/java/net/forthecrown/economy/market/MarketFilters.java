package net.forthecrown.economy.market;

import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.utils.transformation.BlockCopyInfo;
import net.forthecrown.utils.transformation.BlockFilter;
import net.forthecrown.utils.transformation.RegionCopyPaste;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.persistence.PersistentDataType;

class MarketFilters {
    public static class IgnoreCopyEntrance implements BlockFilter {
        @Override
        public boolean test(BlockCopyInfo info, RegionCopyPaste paste, boolean async) {
            return IgnorePasteEntrance.checkBlock(info.copy().getState());
        }
    }

    public static class IgnorePasteEntrance implements BlockFilter {
        @Override
        public boolean test(BlockCopyInfo info, RegionCopyPaste paste, boolean async) {
            return checkBlock(info.paste().getState());
        }

        static boolean checkBlock(BlockState state) {
            if(!(state instanceof Sign)) return true;
            Sign sign = (Sign) state;

            return !sign.getPersistentDataContainer().has(ShopEntrance.DOOR_SIGN, PersistentDataType.BYTE);
        }
    }

    public static class IgnoreNotice implements BlockFilter {
        @Override
        public boolean test(BlockCopyInfo info, RegionCopyPaste paste, boolean async) {
            BlockState state = info.paste().getState();

            if(!(state instanceof Skull)) return true;
            Skull skull = (Skull) state;

            return !skull.getPersistentDataContainer().has(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING);
        }
    }

    public static class IgnoreShop implements BlockFilter {
        @Override
        public boolean test(BlockCopyInfo info, RegionCopyPaste paste, boolean async) {
            return !ShopManager.isShop(info.copy());
        }
    }
}
