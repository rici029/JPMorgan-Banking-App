package org.poo.splitPayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.appOperations.ExchangeOperations;
import org.poo.transactions.TransactionErrorSplitPayment;
import org.poo.transactions.TransactionSplitPayment;
import org.poo.transactions.Transactions;
import org.poo.user.User;
import org.poo.utils.Utils;


import java.util.HashMap;
import java.util.List;

@Getter @Setter
public class SplitPayment {
    private List<String> accounts;
    private double amount;
    private String type;
    private int timestamp;
    private String currency;
    private int noOfAccepts;

    public SplitPayment(final List<String> accounts, final double amount, final String type,
                        final int timestamp, final String currency) {
        this.accounts = accounts;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.currency = currency;
        this.noOfAccepts = 0;
    }

    /**
     * Method that starts the payment process for a split payment.
     * @param exchangeRates exchange rates
     * @param accountMap account map
     * @param usersAccountMap users account map
     */
    public void startPayment(final HashMap<String, HashMap<String, Double>> exchangeRates,
                             final HashMap<String, Account> accountMap,
                             final HashMap<String, User> usersAccountMap) {
        double amountPerAccount = amount / accounts.size();
        String errorIBAN = null;

        for (String iban : accounts) {
            Account account = accountMap.get(iban);
            if (account.getCurrency().equals(this.getCurrency())) {
                if (account.getBalance() < amountPerAccount) {
                    errorIBAN = iban;
                }
            } else {
                double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                        this.getCurrency(), account.getCurrency());
                double convertedAmount = amountPerAccount * exchangeRate;
                if (account.getBalance() < convertedAmount) {
                    errorIBAN = iban;
                }
            }
        }
        if (errorIBAN != null) {
            Account errorAccount = accountMap.get(errorIBAN);
            errorSplitPayment(errorAccount, usersAccountMap,
                    amountPerAccount, accountMap);
            return;
        }

        for (String iban : accounts) {
            payBill(accountMap.get(iban), amountPerAccount, exchangeRates);
            addSplitTransaction(amountPerAccount, usersAccountMap.get(iban), accountMap.get(iban));
        }
    }

    /**
     *
     * @param account account that pays the bill
     * @param amountPerAccount amountPerAccount to be paid
     * @param exchangeRates exchange rates
     */
    private void payBill(final Account account, final double amountPerAccount,
                         final HashMap<String, HashMap<String, Double>> exchangeRates) {
        if (account.getCurrency().equals(currency)) {
            account.pay(amountPerAccount);
        } else {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    currency, account.getCurrency());
            if (exchangeRate == -1) {
                return;
            }
            double convertedAmount = amountPerAccount * exchangeRate;
            account.pay(convertedAmount);
        }
    }

    /**
     *
     * @param errorAccount account that has insufficient funds
     * @param usersAccountMap map of users and their accounts
     * @param amountPerAccount amount per account
     * @param accountMap map of accounts
     */
    private void errorSplitPayment(final Account errorAccount,
                                         final HashMap<String, User> usersAccountMap,
                                         final double amountPerAccount,
                                         final HashMap<String, Account> accountMap) {
        for (String iban : accounts) {
            Account account = accountMap.get(iban);
            String description = String.format("Split payment of %.2f %s", amount, currency);
            String error = "Account " + errorAccount.getIban()
                    + " has insufficient funds for a split payment.";
            Transactions transaction = new TransactionErrorSplitPayment(description,
                    timestamp, amountPerAccount, currency,
                    accounts, error, type);
            User user = usersAccountMap.get(account.getIban());
            Utils.putTransactionInRightPlace(user.getTransactions(), transaction);
            Utils.putTransactionInRightPlace(account.getTransactions(), transaction);
        }
    }

    /**
     *
     * @param amountPerAccount amount per account
     * @param user user that pays the bill
     * @param account account that pays the bill
     */
    private void addSplitTransaction(final double amountPerAccount, final User user,
                                            final Account account) {
        String description = String.format("Split payment of %.2f %s", amount,
                currency);
        Transactions transaction = new TransactionSplitPayment(amountPerAccount,
                currency, accounts, description, timestamp, type);
        Utils.putTransactionInRightPlace(user.getTransactions(), transaction);
        Utils.putTransactionInRightPlace(account.getTransactions(), transaction);
    }

    /**
     * Method that adds a rejected transaction to the accounts and users.
     * @param accountMap account map
     * @param usersAccountMap users account map
     */
    public void rejected(final HashMap<String, Account> accountMap,
                         final HashMap<String, User> usersAccountMap) {
        for (String iban : accounts) {
            Account account = accountMap.get(iban);
            String description = String.format("Split payment of %.2f %s", amount, currency);
            Transactions transaction = new TransactionErrorSplitPayment(description,
                    timestamp, amount, currency, accounts, "One user rejected the payment.", type);
            User user = usersAccountMap.get(account.getIban());
            Utils.putTransactionInRightPlace(user.getTransactions(), transaction);
            Utils.putTransactionInRightPlace(account.getTransactions(), transaction);
        }
    }
}

