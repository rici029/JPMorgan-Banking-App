package org.poo.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountSavings extends Account {
    private double interestRate;

    public AccountSavings(final String email, final String currency,
                          final String accountType, final double interestRate) {
        super(email, currency, accountType);
        this.interestRate = interestRate;
    }
}
