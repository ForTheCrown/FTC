package net.forthecrown.economy;

/**
 * Represents something which can be taxed
 */
public interface Taxable {
    boolean isTaxed();
    void setTaxed(boolean taxed);
}