package net.forthecrown.utils.inventory.menu;

import net.forthecrown.user.User;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public interface MenuNode extends MenuNodeItem, MenuClickConsumer {
    static Builder builder() {
        return new Builder();
    }

    static MenuNode of(MenuClickConsumer click, ItemStack item) {
        return builder()
                .setRunnable(click)
                .setItem(item)
                .build();
    }

    static MenuNode of(ItemStack item) {
        return builder()
                .setItem(item)
                .build();
    }

    class Builder {
        private MenuNodeItem item;
        private MenuClickConsumer runnable;
        private boolean playSound = true;

        public Builder setItem(MenuNodeItem item) {
            this.item = item;
            return this;
        }

        public Builder setItem(ItemStack item) {
            return setItem(MenuNodeItem.of(item));
        }

        public Builder setItem(Function<User, ItemStack> provider) {
            return setItem(MenuNodeItem.of(provider));
        }

        public Builder setRunnable(MenuClickConsumer runnable) {
            this.runnable = runnable;
            return this;
        }

        public Builder setRunnable(Contextless runnable) {
            this.runnable = runnable;
            return this;
        }

        public Builder setPlaySound(boolean b) {
            this.playSound = b;
            return this;
        }

        public MenuNode build() {
            return new BuiltNode(item, runnable, playSound);
        }
    }
}