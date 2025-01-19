package org.poo.account;

public final class AccountFactory {
    private AccountFactory() {
        // not called
    }

    /**
     *
     * @param email email of the user
     * @param currency currency of the account
     * @param accountType type of the account
     * @param interestRate interest rate of the account
     * @return
     */
    public static Account createAccount(final String email, final String currency,
                                        final String accountType, final double interestRate) {
        switch (accountType) {
            case "classic":
                return new AccountClassic(email, currency, accountType);
            case "savings":
                return new AccountSavings(email, currency, accountType, interestRate);
            case "business":
                return new BusinessAccount(email, currency, accountType);
            default:
                return null;
        }
    }
}
