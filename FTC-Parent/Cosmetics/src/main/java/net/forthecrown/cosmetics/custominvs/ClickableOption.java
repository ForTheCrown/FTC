package net.forthecrown.cosmetics.custominvs;

public class ClickableOption extends Option {

    private Runnable actionOnClick;
    private int cd = 0;

    public Runnable getAction() { return this.actionOnClick; }
    public void setActionOnClick(Runnable action) { this.actionOnClick = action; }

    public int getCooldown() { return this.cd; }
    public void setCooldown(int cd) { this.cd = cd; }


    @Override
    public void handleClick() {
        getAction().run();
    }
}
