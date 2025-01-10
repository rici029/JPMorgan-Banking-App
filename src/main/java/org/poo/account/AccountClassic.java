package org.poo.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountClassic extends Account {
    public AccountClassic(final String email, final String currency, final String accountType) {
        super(email, currency, accountType);
    }
}
