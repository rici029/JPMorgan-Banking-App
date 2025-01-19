package org.poo.commerciant;

import java.util.ArrayList;

public final class CommerciantOperation {
    private CommerciantOperation() {
    }

    public static Commerciant findCommerciant(final String commerciant, final ArrayList<Commerciant> commerciants) {
        for (Commerciant c : commerciants)
            if (c.getName().equals(commerciant))
                return c;
        return null;
    }

    public static Commerciant findCommerciantWithIban(final String iban, final ArrayList<Commerciant> commerciants) {
        for (Commerciant c : commerciants)
            if (c.getIban().equals(iban))
                return c;
        return null;
    }
}
