package org.poo.commerciant;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Commerciant {
    private String name;
    private String iban;
    private String type;
    private String cashbackStrategy;

    public Commerciant(final String name, final String iban, final String type, final String cashbackStrategy) {
        this.name = name;
        this.iban = iban;
        this.type = type;
        this.cashbackStrategy = cashbackStrategy;
    }
}
