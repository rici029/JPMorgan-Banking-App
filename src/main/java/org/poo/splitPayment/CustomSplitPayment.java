package org.poo.splitPayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.appOperations.ExchangeOperations;
import org.poo.transactions.*;
import org.poo.user.User;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter @Setter
public class CustomSplitPayment extends SplitPayment {
    private List<Double> amountForUsers;
    public CustomSplitPayment(final List<String> accounts, final double amount, final String type,
                              final int timestamp, final String currency,
                              final List<Double> amountForUsers) {
        super(accounts, amount, type, timestamp, currency);
        this.amountForUsers = amountForUsers;
    }

    public void startPayment(HashMap<String, HashMap<String, Double>> exchangeRates,
                             HashMap<String, Account> accountMap, HashMap<String, User> usersAccountMap) {
        List<String> accounts = this.getAccounts();
        String errorIBAN = null;
        for (int i = 0; i < accounts.size(); i++) {
            String iban = accounts.get(i);
            double amountPerAccount = amountForUsers.get(i);
            Account account = accountMap.get(iban);
            if (account.getCurrency().equals(this.getCurrency())) {
                if (account.getBalance() < amountPerAccount) {
                    errorIBAN = iban;
                    break;
                }
            } else {
                double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                        this.getCurrency(), account.getCurrency());
                double convertedAmount = amountPerAccount * exchangeRate;
                if (account.getBalance() < convertedAmount) {
                    errorIBAN = iban;
                    break;
                }
            }
        }
        if(errorIBAN != null) {
            Account errorAccount = accountMap.get(errorIBAN);
            errorSplitPayment(accounts, errorAccount, usersAccountMap, accountMap);
            return;
        }

        for(String iban : accounts) {
            payBill(accountMap.get(iban), getCurrency(), exchangeRates);
            addSplitTransaction(amountForUsers.get(accounts.indexOf(iban)),
                    usersAccountMap.get(iban), accountMap.get(iban));
        }
    }

    /**
     *
     * @param account account that pays the bill
     * @param currency currency of the amount
     * @param exchangeRates exchange rates
     */
    private void payBill(final Account account, final String currency,
                         final HashMap<String, HashMap<String, Double>> exchangeRates) {
        double amount = amountForUsers.get(getAccounts().indexOf(account.getIban()));
        if (account.getCurrency().equals(currency)) {
            account.pay(amount);
        } else {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    currency, account.getCurrency());
            if (exchangeRate == -1) {
                return;
            }
            double convertedAmount = amount * exchangeRate;
            account.pay(convertedAmount);
        }
    }

    /**
     *
     * @param accounts list of accounts to split the payment
     * @param errorAccount account that has insufficient funds
     * @param usersAccountMap map of users and their accounts
     * @param accountMap map of accounts
     */
    private void errorSplitPayment(final List<String> accounts, final Account errorAccount,
                                   final HashMap<String, User> usersAccountMap,
                                   final HashMap<String, Account> accountMap) {
        for (String iban : accounts) {
            Account account = accountMap.get(iban);
            String description = String.format("Split payment of %.2f %s", getAmount(), getCurrency());
            String error = "Account " + errorAccount.getIban() +
                    " has insufficient funds for a split payment.";
            Transactions transaction = new TransactionErrorCustomSplit(description, getTimestamp(),
                    amountForUsers, getCurrency(), accounts, error, getType());
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
        String description = String.format("Split payment of %.2f %s", getAmount(),
                getCurrency());
        Transactions transaction = new TransactionCustomSplitPayment(description, getTimestamp(),
                amountForUsers, getCurrency(), getAccounts(), getType());
        Utils.putTransactionInRightPlace(user.getTransactions(), transaction);
        Utils.putTransactionInRightPlace(account.getTransactions(), transaction);
    }

    /**
     * Method called when a user rejects the payment.
     * @param accountMap map of accounts
     * @param usersAccountMap map of users and their accounts
     */
    public void rejected(final HashMap<String, Account> accountMap,
                         final HashMap<String, User> usersAccountMap) {
        for(String iban : getAccounts()) {
            Account account = accountMap.get(iban);
            String description = String.format("Split payment of %.2f %s", getAmount(), getCurrency());
            Transactions transaction = new TransactionErrorCustomSplit(description, getTimestamp(),
                    amountForUsers, getCurrency(), getAccounts(), "One user rejected the payment.", getType());
            User user = usersAccountMap.get(account.getIban());
            Utils.putTransactionInRightPlace(user.getTransactions(), transaction);
            Utils.putTransactionInRightPlace(account.getTransactions(), transaction);
        }
    }
}
