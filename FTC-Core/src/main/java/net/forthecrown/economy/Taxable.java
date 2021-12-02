package net.forthecrown.economy;

public interface Taxable {
    boolean hasBeenTaxed();
    void setTaxed(boolean taxed);
}
