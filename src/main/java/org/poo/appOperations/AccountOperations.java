package org.poo.appOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.AccountSavings;
import org.poo.card.Card;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.TransactionCard;
import org.poo.transactions.TransactionPayment;
import org.poo.transactions.TransactionSplitPayment;
import org.poo.transactions.Transactions;
import org.poo.transactions.TransactionErrorSplitPayment;
import org.poo.transactions.TransactionTransfer;
import org.poo.user.User;
import org.poo.account.Account;
import org.poo.account.AccountFactory;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class AccountOperations {
    private AccountOperations() {
        //not called
    }

    private static void addTransactionToObservers(final Transactions transaction,
                                                  final User user, final Account account) {
        transaction.registerObserver(user);
        transaction.registerObserver(account);
        transaction.notifyObservers();
    }

    /**
     *
     * @param users list of users
     * @param command command input
     * @param usersAccountsMap map of users and their accounts
     * @param accountMap map of accounts
     */
    public static void addAccount(final ArrayList<User> users, final CommandInput command,
                                  final HashMap<String, User> usersAccountsMap,
                                  final HashMap<String, Account> accountMap) {
        String email = command.getEmail();
        String currency = command.getCurrency();
        String accountType = command.getAccountType();
        double interestRate = command.getInterestRate();
        Account account = AccountFactory.createAccount(email, currency, accountType, interestRate);
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                user.addAccount(account);
                usersAccountsMap.put(account.getIban(), user);
                accountMap.put(account.getIban(), account);
                Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "New account created");
                addTransactionToObservers(transaction, user, account);
                break;
            }
        }
    }

    /**
     *
     * @param usersAccountsMap map of users and their accounts
     * @param command command input
     */
    public static void addFunds(final HashMap<String, User> usersAccountsMap,
                                final CommandInput command) {
        String iban = command.getAccount();
        double amount = command.getAmount();
        if (!usersAccountsMap.containsKey(iban)) {
            return;
        }
        User user = usersAccountsMap.get(iban);
        for (Account account : user.getAccounts()) {
            if (account.getIban().equals(iban)) {
                account.deposit(amount);
                break;
            }
        }
    }

    /**
     *
     * @param usersAccountsMap map of users and their accounts
     * @param command command input
     * @param output output array
     */
    public static void deleteAccount(final HashMap<String, User> usersAccountsMap,
                                     final CommandInput command, final ArrayNode output) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("command", command.getCommand());
        String iban = command.getAccount();
        if (!usersAccountsMap.containsKey(iban)) {
            return;
        }

        User user = usersAccountsMap.get(iban);
        for (Account account : user.getAccounts()) {
            if (account.getIban().equals(iban)) {
                if (account.getBalance() == 0) {
                    user.removeAccount(account);
                    usersAccountsMap.remove(iban);
                    output.add(PrintOperations.printSuccessJson(command));
                    break;
                } else {
                    ObjectNode error = mapper.createObjectNode();
                    error.put("error",
                            "Account couldn't be deleted - see org.poo.transactions for details");
                    error.put("timestamp", command.getTimestamp());
                    response.set("output", error);
                    response.put("timestamp", command.getTimestamp());
                    output.add(response);
                    Transactions transaction = new TransactionAction(command.getTimestamp(),
                            "Account couldn't be deleted - there are funds remaining");
                    addTransactionToObservers(transaction, user, account);
                }
            }
        }
    }

    /**
     *
     * @param usersAccountsMap map of users and their accounts
     * @param command command input
     */
    public static void setMinimumBalance(final HashMap<String, User> usersAccountsMap,
                                         final CommandInput command) {
        String iban = command.getAccount();
        double minimumBalance = command.getMinBalance();
        if (!usersAccountsMap.containsKey(iban)) {
            return;
        }
        User user = usersAccountsMap.get(iban);
        for (Account account : user.getAccounts()) {
            if (account.getIban().equals(iban)) {
                account.setMinimumBalance(minimumBalance);
                break;
            }
        }
    }

    /**
     *
     * @param command command input
     * @param exchangeRates exchange rates
     * @param cardAccountMap map of cards and accounts
     * @param output output array
     * @param usersAccountMap map of users and their accounts
     * @param usersCardsMap map of users and their cards
     */
    public static void payOnline(final CommandInput command, final HashMap<String,
                                 HashMap<String, Double>> exchangeRates,
                                 final HashMap<String, Account> cardAccountMap,
                                 final ArrayNode output,
                                 final HashMap<String, User> usersAccountMap,
                                 final HashMap<String, User> usersCardsMap) {
        double convertedAmount;
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        if (!cardAccountMap.containsKey(cardNumber)) {
            PrintOperations.cardNotFound(output, command);
            return;
        }
        Account account = cardAccountMap.get(cardNumber);
        User user = usersAccountMap.get(account.getIban());
        Card card = null;
        for (Card c : account.getCards()) {
            if (c.getCardNumber().equals(cardNumber)) {
                card = c;
                break;
            }
        }

        if (card == null) {
            PrintOperations.cardNotFound(output, command);
            return;
        }

        if (card.getCardStatus().equals("frozen")) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "The card is frozen");
            addTransactionToObservers(transaction, user, account);
            return;
        }

        String toCurrency = account.getCurrency();

        String fromCurrency = command.getCurrency();

        if (fromCurrency.equals(toCurrency)) {
            if (account.getBalance() - amount < 0) {
                Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "Insufficient funds");
                addTransactionToObservers(transaction, user, account);
                return;
            }
            account.pay(amount);
            Transactions transaction = new TransactionPayment(command.getTimestamp(),
                    "Card payment", amount,
                    command.getCommerciant());
            addTransactionToObservers(transaction, user, account);
        } else {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    fromCurrency, toCurrency);
            convertedAmount = amount * exchangeRate;
            if (account.getBalance() - convertedAmount < 0) {
                Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "Insufficient funds");
                addTransactionToObservers(transaction, user, account);
                return;
            }
            account.pay(convertedAmount);
            Transactions transaction = new TransactionPayment(command.getTimestamp(),
                    "Card payment", convertedAmount,
                    command.getCommerciant());
            addTransactionToObservers(transaction, user, account);
        }
        if (card.getCardType().equals("onetime")) {
            updateOneTimeCard(command, cardAccountMap, usersCardsMap, cardNumber, card,
                    account, user);
        }
    }

    /**
     *
     * @param command command input
     * @param cardAccountMap map of cards and accounts
     * @param usersCardsMap map of users and their cards
     * @param cardNumber card number
     * @param card card to be updated
     * @param account account
     * @param user user
     */
    private static void updateOneTimeCard(final CommandInput command,
                                          final HashMap<String, Account> cardAccountMap,
                                          final HashMap<String, User> usersCardsMap,
                                          final String cardNumber,
                                          final  Card card, final Account account,
                                          final User user) {
        Account mapAccount = cardAccountMap.get(cardNumber);
        cardAccountMap.remove(cardNumber);
        User mapUser = usersCardsMap.get(cardNumber);
        usersCardsMap.remove(cardNumber);
        Transactions transaction = new TransactionCard(card.getCardNumber(),
                account.getIban(), user.getEmail(), "The card has been destroyed",
                command.getTimestamp());
        addTransactionToObservers(transaction, user, account);
        card.setCardNumber(Utils.generateCardNumber());
        Transactions transactionNewCard = new TransactionCard(card.getCardNumber(),
                account.getIban(), user.getEmail(), "New card created",
                command.getTimestamp());
        addTransactionToObservers(transactionNewCard, user, account);
        cardAccountMap.put(card.getCardNumber(), mapAccount);
        usersCardsMap.put(card.getCardNumber(), mapUser);
    }

    /**
     *
     * @param command command input
     * @param exchangeRates exchange rates
     * @param accountMap map of accounts
     * @param usersAccountMap map of users and their accounts
     * @param aliasAccountMap map of alias and accounts
     */
    public static void sendMoney(final CommandInput command, final HashMap<String,
                                    HashMap<String, Double>> exchangeRates,
                                 final HashMap<String, Account> accountMap,
                                 final HashMap<String, User> usersAccountMap,
                                 final HashMap<String, Account> aliasAccountMap) {
        String from = command.getAccount();
        String to = command.getReceiver();
        double amount = command.getAmount();
        User userSender = usersAccountMap.get(from);
        User userReceiver = usersAccountMap.get(to);
        Account toAccount;
        Account fromAccount;
        if (!accountMap.containsKey(from) || !accountMap.containsKey(to)) {
            return;
        }
        if (!aliasAccountMap.containsKey(to)) {
            if (!accountMap.containsKey(to)) {
                return;
            } else {
                toAccount = accountMap.get(to);
            }
        } else {
            toAccount = aliasAccountMap.get(to);
        }
        if (!aliasAccountMap.containsKey(from)) {
            if (!accountMap.containsKey(from)) {
                return;
            } else {
                fromAccount = accountMap.get(from);
            }
        } else {
            fromAccount = aliasAccountMap.get(from);
        }
        if (fromAccount.getBalance() - amount < 0) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Insufficient funds");
            addTransactionToObservers(transaction, userSender, fromAccount);
            return;
        }
        fromAccount.pay(amount);
        String fromCurrency = fromAccount.getCurrency();
        String toCurrency = toAccount.getCurrency();
        double convertedAmount = 0;
        if (fromCurrency.equals(toCurrency)) {
            toAccount.deposit(amount);
        } else {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    fromCurrency, toCurrency);
            convertedAmount = amount * exchangeRate;
            toAccount.deposit(convertedAmount);
        }
        String amountString = String.format("%.1f", amount) + " " + fromCurrency;
        Transactions transaction = new TransactionTransfer(command.getTimestamp(), from, to,
                amountString, command.getDescription(), "sent");
        addTransactionToObservers(transaction, userSender, fromAccount);

        if (convertedAmount != 0) {
            if (Math.floor(convertedAmount) == convertedAmount) {
                amountString = String.format("%.1f", convertedAmount) + " " + toCurrency;
            } else {
                amountString = convertedAmount + " " + toCurrency;
            }
        }
        Transactions transactionReceiver = new TransactionTransfer(command.getTimestamp(),
                from, to, amountString, command.getDescription(), "received");
        addTransactionToObservers(transactionReceiver, userReceiver, toAccount);
    }

    /**
     *
     * @param accountMap map of accounts
     * @param command command input
     * @param aliasAccountMap map of alias and accounts
     */
    public static void setAlias(final HashMap<String, Account> accountMap,
                                final CommandInput command,
                                final HashMap<String, Account> aliasAccountMap) {
        String iban = command.getAccount();
        String alias = command.getAlias();
        if (!accountMap.containsKey(iban)) {
            return;
        }
        Account account = accountMap.get(iban);
        account.setAlias(alias);
        aliasAccountMap.put(alias, account);
    }

    /**
     *
     * @param command command input
     * @param exchangeRates exchange rates
     * @param accountMap map of accounts
     * @param usersAccountMap map of users and their accounts
     */
    public static void splitPayment(final CommandInput command,
                                    final HashMap<String, HashMap<String, Double>> exchangeRates,
                                    final HashMap<String, Account> accountMap,
                                    final HashMap<String, User> usersAccountMap) {
        List<String> accounts = command.getAccounts();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int noOfAccounts = accounts.size();
        double amountPerAccount = amount / noOfAccounts;
        String errorIBAN = null;

        for (String iban : accounts) {
            Account account = accountMap.get(iban);
            if (account.getCurrency().equals(currency)) {
                if (account.getBalance() < amountPerAccount) {
                    errorIBAN = iban;
                }
            } else {
                double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                        currency, account.getCurrency());
                double convertedAmount = amountPerAccount * exchangeRate;
                if (account.getBalance() < convertedAmount) {
                    errorIBAN = iban;
                }
            }
        }
        if (errorIBAN != null) {
            Account errorAccount = accountMap.get(errorIBAN);
            errorSplitPayment(accounts, errorAccount, command,
                    usersAccountMap, amountPerAccount, accountMap);
            return;
        }

        for (String iban : accounts) {
            payBill(accountMap.get(iban), amountPerAccount, currency, exchangeRates);
            addSplitTransaction(amountPerAccount, command,
                    usersAccountMap.get(iban), accountMap.get(iban));
        }
    }

    /**
     *
     * @param account account that pays the bill
     * @param amount amount to be paid
     * @param currency currency of the amount
     * @param exchangeRates exchange rates
     */
    private static void payBill(final Account account, final double amount, final String currency,
                                final HashMap<String, HashMap<String, Double>> exchangeRates) {
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
     * @param command command input
     * @param usersAccountMap map of users and their accounts
     * @param amountPerAccount amount per account
     * @param accountMap map of accounts
     */
    private static void errorSplitPayment(final List<String> accounts, final Account errorAccount,
                                          final CommandInput command,
                                          final HashMap<String, User> usersAccountMap,
                                          final double amountPerAccount,
                                          final HashMap<String, Account> accountMap) {
        for (String iban : accounts) {
            Account account = accountMap.get(iban);
            String description = String.format("Split payment of %.2f %s", command.getAmount(),
                    command.getCurrency());
            Transactions transaction = new TransactionErrorSplitPayment(description,
                    command.getTimestamp(), amountPerAccount, command.getCurrency(),
                    command.getAccounts(), errorAccount.getIban());
            User user = usersAccountMap.get(account.getIban());
            addTransactionToObservers(transaction, user, account);
        }
    }

    /**
     *
     * @param amountPerAccount amount per account
     * @param command command input
     * @param user user that pays the bill
     * @param account account that pays the bill
     */
    private static void addSplitTransaction(final double amountPerAccount,
                                            final CommandInput command, final User user,
                                            final Account account) {
        String description = String.format("Split payment of %.2f %s", command.getAmount(),
                command.getCurrency());
        Transactions transaction = new TransactionSplitPayment(amountPerAccount,
                command.getCurrency(), command.getAccounts(), description, command.getTimestamp());
        addTransactionToObservers(transaction, user, account);
    }

    /**
     *
     * @param accountMap map of accounts
     * @param command command input
     * @param output output array
     * @param usersAccountMap map of users and their accounts
     */
    public static void changeInterestRate(final HashMap<String, Account> accountMap,
                                          final CommandInput command, final ArrayNode output,
                                          final HashMap<String, User> usersAccountMap) {
        String iban = command.getAccount();
        double interestRate = command.getInterestRate();
        if (!accountMap.containsKey(iban)) {
            return;
        }
        Account account = accountMap.get(iban);
        if (account.getAccountType().equals("savings")) {
            AccountSavings savingsAccount = (AccountSavings) account;
            savingsAccount.setInterestRate(interestRate);
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Interest rate of the account changed to " + interestRate);
            User user = usersAccountMap.get(iban);
            addTransactionToObservers(transaction, user, account);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", command.getCommand());
            ObjectNode outNode = mapper.createObjectNode();
            outNode.put("timestamp", command.getTimestamp());
            outNode.put("description", "This is not a savings account");
            error.set("output", outNode);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
        }
    }

    /**
     *
     * @param accountMap map of accounts
     * @param command command input
     * @param output output array
     */
    public static void addInterest(final HashMap<String, Account> accountMap,
                                   final CommandInput command, final ArrayNode output) {
        String iban = command.getAccount();
        if (!accountMap.containsKey(iban)) {
            return;
        }
        Account account = accountMap.get(iban);
        if (!account.getAccountType().equals("savings")) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", command.getCommand());
            ObjectNode outNode = mapper.createObjectNode();
            outNode.put("timestamp", command.getTimestamp());
            outNode.put("description", "This is not a savings account");
            error.set("output", outNode);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }
        AccountSavings savingsAccount = (AccountSavings) account;
        savingsAccount.setBalance(savingsAccount.getBalance() + savingsAccount.getInterestRate());
    }
}
