package net.forthecrown.economy;

public interface Taxable {
    boolean isTaxed();
    void setTaxed(boolean taxed);
}