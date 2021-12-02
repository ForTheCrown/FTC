package net.forthecrown.user.actions;

public interface UserAction {
    /**
     * Handles the action in the given action handler
     * @param handler The handler to handle this action
     */
    void handle(UserActionHandler handler);
}
