package net.forthecrown.commands.usables;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.useables.*;

// Accessor which provides a common way of accessing a usage type holder
// this is so I wouldn't have to rewrite the arguments for action holders
// and check holders both.
interface UsageTypeAccessor<T extends  UsageInstance, H extends UsageTypeHolder> {

    UsageTypeAccessor<UsageTest, CheckHolder> CHECKS = new UsageTypeAccessor<>() {
        @Override
        public UsageTypeList getList(CheckHolder holder) {
            return holder.getChecks();
        }

        @Override
        public String getName() {
            return "Test";
        }

        @Override
        public RegistryArguments<UsageType<UsageTest>> getArgumentType() {
            return RegistryArguments.USAGE_CHECK;
        }
    };


    UsageTypeAccessor<UsageAction, ActionHolder> ACTIONS = new UsageTypeAccessor<>() {
        @Override
        public UsageTypeList getList(ActionHolder holder) {
            return holder.getActions();
        }

        @Override
        public String getName() {
            return "Action";
        }

        @Override
        public RegistryArguments<UsageType<UsageAction>> getArgumentType() {
            return RegistryArguments.USAGE_ACTION;
        }
    };

    UsageTypeList<T> getList(H holder);

    String getName();

    RegistryArguments<UsageType<T>> getArgumentType();
}