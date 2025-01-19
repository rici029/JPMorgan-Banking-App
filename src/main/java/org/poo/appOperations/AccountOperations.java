package org.poo.appOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.AccountSavings;
import org.poo.account.BusinessAccount;
import org.poo.businessUser.BusinessUser;
import org.poo.card.Card;
import org.poo.command.concrete.CashWithdrawalCommand;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.transactions.*;
import org.poo.user.User;
import org.poo.account.Account;
import org.poo.account.AccountFactory;
import org.poo.utils.Utils;
import org.poo.commerciant.CommerciantOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class AccountOperations {
    private AccountOperations() {
        //not called
    }

    public static double checkForCommission(final Account account, final double amount,
                                             final String planType,
                                             final HashMap<String,
                                                     HashMap<String, Double>> exchangeRates) {
        double amountInRON = amount;
        if (!account.getCurrency().equals("RON")) {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    account.getCurrency(), "RON");
            amountInRON = amount * exchangeRate;
        }
        if (planType.equals("standard")) {
            return amount * 0.002;
        } else if(planType.equals("silver")){
            if(amountInRON >= 500) {
                return amount * 0.001;
            }
        }
        return 0;

    }

    /**
     * Add a transaction to the observers
     * @param transaction transaction to be added
     * @param user user to be added
     * @param account account to be added
     */
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
                                  final HashMap<String, Account> accountMap,
                                  final HashMap<String, HashMap<String, Double>> exchangeRates) {
        String email = command.getEmail();
        String currency = command.getCurrency();
        String accountType = command.getAccountType();
        double interestRate = command.getInterestRate();
        Account account = AccountFactory.createAccount(email, currency, accountType, interestRate, exchangeRates);
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
                                final CommandInput command, final HashMap<String, Account> accountMap) {
        String iban = command.getAccount();
        double amount = command.getAmount();
        if (!usersAccountsMap.containsKey(iban)) {
            return;
        }
        Account account = accountMap.get(iban);
        if(account.getAccountType().equals("business") && !account.getEmail().equals(command.getEmail())) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            BusinessUser businessUser = null;
            if(businessAccount.getManagers().containsKey(command.getEmail())) {
                businessUser = businessAccount.getManagers().get(command.getEmail());
            } else if(businessAccount.getEmployees().containsKey(command.getEmail())) {
                businessUser = businessAccount.getEmployees().get(command.getEmail());
            }
            if(businessUser == null) {
                return;
            }
            if(businessUser.getRole().equals("employee")) {
                if(businessAccount.getDepositLimit() != 0 && amount > businessAccount.getDepositLimit())
                    return;
            }
            businessUser.getDeposited().put(command.getTimestamp(), amount);
        }
        account.deposit(amount);
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
                if(!account.getEmail().equals(command.getEmail())) {
                    return;
                }
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
                                 final HashMap<String, User> usersCardsMap,
                                 final ArrayList<Commerciant> commerciants) {
        if (command.getAmount() <= 0) {
            return;
        }
        double convertedAmount;
        BusinessAccount businessAccount = null;
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        if (!cardAccountMap.containsKey(cardNumber)) {
            PrintOperations.cardNotFound(output, command);
            return;
        }
        Account account = cardAccountMap.get(cardNumber);

        ObjectMapper mapper = new ObjectMapper();
        if(!account.getAccountType().equals("business") && !account.getEmail().equals(command.getEmail())){
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", command.getCommand());
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("description", "Card not found");
            objectNode.put("timestamp", command.getTimestamp());
            commandOutput.set("output", objectNode);
            commandOutput.put("timestamp", command.getTimestamp());
            output.add(commandOutput);
            return;
        }
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


        if (!card.getEmail().equals(user.getEmail()) && !account.getAccountType().equals("business")){
            PrintOperations.cardNotFound(output, command);
            return;
        }

        if (account.getAccountType().equals("business")) {
            businessAccount = (BusinessAccount) account;
            if(!businessAccount.getManagers().containsKey(command.getEmail()) &&
                    !businessAccount.getEmployees().containsKey(command.getEmail()) &&
                    !account.getEmail().equals(command.getEmail())) {
                PrintOperations.cardNotFound(output, command);
                return;
            }
        }

        if (card.getCardStatus().equals("frozen")) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "The card is frozen");
            addTransactionToObservers(transaction, user, account);
            return;
        }


        if (CommerciantOperation.findCommerciant(command.getCommerciant(), commerciants) == null)
            return;

        String toCurrency = account.getCurrency();

        String fromCurrency = command.getCurrency();

        if (fromCurrency.equals(toCurrency)) {
            double commission = checkForCommission(account, amount, user.getPlan(),
                    exchangeRates);
            if (account.getBalance() - (amount + commission) < 0) {
                Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "Insufficient funds");
                addTransactionToObservers(transaction, user, account);
                return;
            }
            if (account.getAccountType().equals("business") && !account.getEmail().equals(command.getEmail())) {
                BusinessUser businessUser = null;
                if (businessAccount.getManagers().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getManagers().get(command.getEmail());
                } else if (businessAccount.getEmployees().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getEmployees().get(command.getEmail());
                }

                if (businessUser.getRole().equals("employee")) {
                    if(businessAccount.getSpendingLimit() != 0 && amount > businessAccount.getSpendingLimit())
                        return;
                }

                businessUser.getSpent().put(command.getTimestamp(), amount);
                HashMap<Integer, String> spendings = businessUser.getToCommerciantsSpendingList();
                spendings.put(command.getTimestamp(), command.getCommerciant());
                if (!businessAccount.getCommerciants().contains(command.getCommerciant()))
                    businessAccount.getCommerciants().add(command.getCommerciant());
            }
            account.pay(amount);
            CashbackOperations.getCashback(account,
                    CommerciantOperation.findCommerciant(command.getCommerciant(), commerciants),
                    amount, user, exchangeRates);
            account.pay(commission);
            Transactions transaction = new TransactionPayment(command.getTimestamp(),
                    "Card payment", amount,
                    command.getCommerciant());
            addTransactionToObservers(transaction, user, account);
            double amountInRON = amount;
            if (!account.getCurrency().equals("RON")) {
                double exchangeRateRON = ExchangeOperations.getExchangeRate(exchangeRates,
                        account.getCurrency(), "RON");
                amountInRON = amount * exchangeRateRON;
            }
            if(amountInRON >= 300 && user.getPlan().equals("silver")) {
                user.setNrOfPaymentsForUpgrade(user.getNrOfPaymentsForUpgrade() + 1);
                if(user.getNrOfPaymentsForUpgrade() == 5) {
                    user.setPlan("gold");
                    Transactions transactionUpgrade = new TransactionUpdatePlan(account.getIban(),
                            "gold", "Upgrade plan", command.getTimestamp());
                    addTransactionToObservers(transactionUpgrade, user, account);
                }
            }
        } else {
            double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                    fromCurrency, toCurrency);
            convertedAmount = amount * exchangeRate;
            double commission = checkForCommission(account, convertedAmount, user.getPlan(),
                    exchangeRates);
            if (account.getBalance() - (convertedAmount + commission) < 0) {
                Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "Insufficient funds");
                addTransactionToObservers(transaction, user, account);
                return;
            }
            if(account.getAccountType().equals("business") && !account.getEmail().equals(command.getEmail())) {
                businessAccount = (BusinessAccount) account;
                BusinessUser businessUser = null;
                if(businessAccount.getManagers().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getManagers().get(command.getEmail());
                } else if(businessAccount.getEmployees().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getEmployees().get(command.getEmail());
                }

                if(businessUser.getRole().equals("employee")) {
                    if(businessAccount.getSpendingLimit() != 0 && convertedAmount > businessAccount.getSpendingLimit())
                        return;
                }
                businessUser.getSpent().put(command.getTimestamp(), convertedAmount);
                HashMap<Integer, String> spendings = businessUser.getToCommerciantsSpendingList();
                spendings.put(command.getTimestamp(), command.getCommerciant());
                if(!businessAccount.getCommerciants().contains(command.getCommerciant()))
                    businessAccount.getCommerciants().add(command.getCommerciant());
            }
            account.pay(convertedAmount);
            CashbackOperations.getCashback(account, CommerciantOperation.findCommerciant(command.getCommerciant(), commerciants), convertedAmount, user,
                    exchangeRates);
            account.pay(commission);
            Transactions transaction = new TransactionPayment(command.getTimestamp(),
                    "Card payment", convertedAmount,
                    command.getCommerciant());
            addTransactionToObservers(transaction, user, account);
            double amountInRON = amount;
            if (!account.getCurrency().equals("RON")) {
                double exchangeRateRON = ExchangeOperations.getExchangeRate(exchangeRates,
                        account.getCurrency(), "RON");
                amountInRON = amount * exchangeRateRON;
            }
            if(amountInRON >= 300 && user.getPlan().equals("silver")) {
                user.setNrOfPaymentsForUpgrade(user.getNrOfPaymentsForUpgrade() + 1);
                if(user.getNrOfPaymentsForUpgrade() == 5) {
                    user.setPlan("gold");
                    Transactions transactionUpgrade = new TransactionUpdatePlan(account.getIban(),
                            "gold", "Upgrade plan", command.getTimestamp());
                    addTransactionToObservers(transactionUpgrade, user, account);
                }
            }
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
                                 final HashMap<String, Account> aliasAccountMap,
                                 final ArrayNode output, final ArrayList<Commerciant> commerciants,
                                 final HashMap<String, User> usersMap) {
        String from = command.getAccount();
        String to = command.getReceiver();
        double amount = command.getAmount();
        User userSender = usersMap.get(command.getEmail());
        ObjectMapper mapper = new ObjectMapper();
        if (userSender == null) {
            ObjectNode error = mapper.createObjectNode();
            error.put("command", command.getCommand());
            ObjectNode outNode = mapper.createObjectNode();
            outNode.put("timestamp", command.getTimestamp());
            outNode.put("description", "User not found");
            error.set("output", outNode);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }

        Account toAccount = null;
        Account fromAccount;
        if (!aliasAccountMap.containsKey(from)) {
            if (!accountMap.containsKey(from)) {
                ObjectNode error = mapper.createObjectNode();
                error.put("command", command.getCommand());
                ObjectNode outNode = mapper.createObjectNode();
                outNode.put("timestamp", command.getTimestamp());
                outNode.put("description", "User not found");
                error.set("output", outNode);
                error.put("timestamp", command.getTimestamp());
                output.add(error);
                return;
            } else {
                fromAccount = accountMap.get(from);
            }
        } else {
            fromAccount = aliasAccountMap.get(from);
        }
        Commerciant commerciant = CommerciantOperation.findCommerciantWithIban(to, commerciants);
        User userReceiver = null;
        if (commerciant == null) {
            if (!aliasAccountMap.containsKey(to)) {
                if (!accountMap.containsKey(to)) {
                    ObjectNode error = mapper.createObjectNode();
                    error.put("command", command.getCommand());
                    ObjectNode outNode = mapper.createObjectNode();
                    outNode.put("timestamp", command.getTimestamp());
                    outNode.put("description", "User not found");
                    error.set("output", outNode);
                    error.put("timestamp", command.getTimestamp());
                    output.add(error);
                    return;
                } else {
                    toAccount = accountMap.get(to);
                    userReceiver = usersAccountMap.get(toAccount.getIban());
                }
            } else {
                toAccount = aliasAccountMap.get(to);
                userReceiver = usersAccountMap.get(toAccount.getIban());
            }
        }

        if (userReceiver == null && commerciant == null) {
            ObjectNode error = mapper.createObjectNode();
            error.put("command", command.getCommand());
            ObjectNode outNode = mapper.createObjectNode();
            outNode.put("timestamp", command.getTimestamp());
            outNode.put("description", "User not found");
            error.set("output", outNode);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }
        double commissionSender = checkForCommission(fromAccount, amount, userSender.getPlan(),
                exchangeRates);
        if (fromAccount.getBalance() - (amount + commissionSender) < 0) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Insufficient funds");
            addTransactionToObservers(transaction, userSender, fromAccount);
            return;
        }

        BusinessAccount businessAccount = null;

        if(fromAccount.getAccountType().equals("business"))
            businessAccount = (BusinessAccount) fromAccount;
        if(fromAccount.getAccountType().equals("business") && !fromAccount.getEmail().equals(command.getEmail())) {
            BusinessUser businessUser = null;
            if(businessAccount.getManagers().containsKey(command.getEmail())) {
                businessUser = businessAccount.getManagers().get(command.getEmail());
            } else if(businessAccount.getEmployees().containsKey(command.getEmail())) {
                businessUser = businessAccount.getEmployees().get(command.getEmail());
            }
            if(businessUser == null) {
                return;
            }
            if(businessUser.getRole().equals("employee")) {
                if(businessAccount.getSpendingLimit() != 0 && amount > businessAccount.getSpendingLimit())
                    return;
            }
            businessUser.getSpent().put(command.getTimestamp(), amount);
        }
        fromAccount.pay(amount);
        fromAccount.pay(commissionSender);
        if(commerciant != null) {
            if(businessAccount != null && !fromAccount.getEmail().equals(command.getEmail())) {
                BusinessUser businessUser = null;
                if(businessAccount.getManagers().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getManagers().get(command.getEmail());
                } else if(businessAccount.getEmployees().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getEmployees().get(command.getEmail());
                }
                if(businessUser == null) {
                    return;
                }
                HashMap<Integer, String> spendings = businessUser.getToCommerciantsSpendingList();
                spendings.put(command.getTimestamp(), commerciant.getName());
                if(!businessAccount.getCommerciants().contains(commerciant.getName()))
                    businessAccount.getCommerciants().add(commerciant.getName());
            }
            CashbackOperations.getCashback(fromAccount, commerciant, amount, userSender,
                    exchangeRates);
            Transactions transaction = new TransactionTransfer(command.getTimestamp(), from,
                    to, amount + " " + fromAccount.getCurrency(), command.getDescription(),
                    "sent");
            addTransactionToObservers(transaction, userSender, fromAccount);
            return;
        }
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
                                   final CommandInput command, final ArrayNode output,
                                   final HashMap<String, User> usersAccountMap) {
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
        double interest = savingsAccount.getInterestRate() * savingsAccount.getBalance();
        savingsAccount.setBalance(savingsAccount.getBalance() + interest);
        Transactions transaction = new TransactionInterestRate(command.getTimestamp(),
                "Interest rate income", interest, savingsAccount.getCurrency());
        User user = usersAccountMap.get(iban);
        addTransactionToObservers(transaction, user, account);
    }
}
