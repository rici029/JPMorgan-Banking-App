package org.poo.commerciant;

import java.util.ArrayList;

public final class CommerciantOperation {
    private CommerciantOperation() {
    }

    /**
     * Method that finds a commerciant by name.
     * @param commerciant the commerciant
     * @param commerciants the list of commerciants
     * @return the commerciant
     */
    public static Commerciant findCommerciant(final String commerciant,
                                              final ArrayList<Commerciant> commerciants) {
        for (Commerciant c : commerciants) {
            if (c.getName().equals(commerciant)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Method that finds a commerciant by IBAN.
     * @param iban the IBAN
     * @param commerciants the list of commerciants
     * @return the commerciant
     */
    public static Commerciant findCommerciantWithIban(final String iban,
                                                      final ArrayList<Commerciant> commerciants) {
        for (Commerciant c : commerciants) {
            if (c.getIban().equals(iban)) {
                return c;
            }
        }
        return null;
    }
}
