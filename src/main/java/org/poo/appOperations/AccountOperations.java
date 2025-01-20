package org.poo.appOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.BusinessAccount;
import org.poo.businessUser.BusinessUser;
import org.poo.card.Card;
import org.poo.commerciant.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.TransactionPayment;
import org.poo.transactions.TransactionTransfer;
import org.poo.transactions.TransactionUpdatePlan;
import org.poo.transactions.Transactions;
import org.poo.transactions.TransactionCard;
import org.poo.user.User;
import org.poo.account.Account;

import org.poo.utils.Utils;
import org.poo.commerciant.CommerciantOperation;

import java.util.ArrayList;
import java.util.HashMap;


public final class AccountOperations {
    private AccountOperations() {
        //not called
    }
    private static final double COMMISION_STANDARD = 0.002;
    private static final double COMMISION_SILVER = 0.001;
    private static final int MAXIMUM_PAYMENTS_FOR_UPGRADE = 5;
    private static final int LIMIT_FOR_SILVER = 500;
    private static final int LIMIT_FOR_UPGRADE = 300;

    /**
     * Check for commission
     * @param account account to be checked
     * @param amount amount to be checked
     * @param planType plan type
     * @param exchangeRates exchange rates
     * @return the commission
     */
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
            return amount * COMMISION_STANDARD;
        } else if (planType.equals("silver")) {
            if (amountInRON >= LIMIT_FOR_SILVER) {
                return amount * COMMISION_SILVER;
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
        BusinessAccount businessAccount = null;
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        if (!cardAccountMap.containsKey(cardNumber)) {
            PrintOperations.cardNotFound(output, command);
            return;
        }
        Account account = cardAccountMap.get(cardNumber);

        if (!account.getAccountType().equals("business")
                && !account.getEmail().equals(command.getEmail())) {
            PrintOperations.cardNotFound(output, command);
            return;
        }
        User user = usersAccountMap.get(account.getIban());
        Card card = account.getCards().stream().filter(c ->
                c.getCardNumber().equals(cardNumber)).findFirst().orElse(null);

        if (card == null) {
            PrintOperations.cardNotFound(output, command);
            return;
        }


        if (!card.getEmail().equals(user.getEmail())
                && !account.getAccountType().equals("business")) {
            PrintOperations.cardNotFound(output, command);
            return;
        }

        if (account.getAccountType().equals("business")) {
            businessAccount = (BusinessAccount) account;
            if (!businessAccount.getManagers().containsKey(command.getEmail())
                    && !businessAccount.getEmployees().containsKey(command.getEmail())
                    && !account.getEmail().equals(command.getEmail())) {
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

        if (CommerciantOperation.findCommerciant(command.getCommerciant(), commerciants) == null) {
            return;
        }
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
            if (account.getAccountType().equals("business")
                    && !account.getEmail().equals(command.getEmail())) {
                BusinessUser businessUser = null;
                if (businessAccount.getManagers().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getManagers().get(command.getEmail());
                } else if (businessAccount.getEmployees().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getEmployees().get(command.getEmail());
                }

                if (businessUser.getRole().equals("employee")) {
                    if (businessAccount.getSpendingLimit() != 0
                            && amount > businessAccount.getSpendingLimit()) {
                        return;
                    }
                }
                businessUser.getSpent().put(command.getTimestamp(), amount);
                HashMap<Integer, String> spendings = businessUser.getToCommerciantsSpendingList();
                spendings.put(command.getTimestamp(), command.getCommerciant());
                if (!businessAccount.getCommerciants().contains(command.getCommerciant())) {
                    businessAccount.getCommerciants().add(command.getCommerciant());
                }
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
            if (amountInRON >= LIMIT_FOR_UPGRADE && user.getPlan().equals("silver")) {
                user.setNrOfPaymentsForUpgrade(user.getNrOfPaymentsForUpgrade() + 1);
                if (user.getNrOfPaymentsForUpgrade() == MAXIMUM_PAYMENTS_FOR_UPGRADE) {
                    user.setPlan("gold");
                    Transactions transactionUpgrade = new TransactionUpdatePlan(account.getIban(),
                            "gold", "Upgrade plan", command.getTimestamp());
                    addTransactionToObservers(transactionUpgrade, user, account);
                }
            }
        } else {
            if (convertedPayment(command, exchangeRates,
                    commerciants, fromCurrency, toCurrency, amount, account, user)) {
                return;
            }
        }
        if (card.getCardType().equals("onetime")) {
            updateOneTimeCard(command, cardAccountMap, usersCardsMap, cardNumber, card,
                    account, user);
        }
    }

    /**
     * Converted online payment
     * @param command command input
     * @param exchangeRates exchange rates
     * @param commerciants list of commerciants
     * @param fromCurrency from currency
     * @param toCurrency to currency
     * @param amount amount
     * @param account account
     * @param user user
     * @return
     */
    private static boolean convertedPayment(final CommandInput command,
                                            final HashMap<String,
                                                    HashMap<String, Double>> exchangeRates,
                                            final ArrayList<Commerciant> commerciants,
                                            final String fromCurrency,
                                            final String toCurrency,
                                            final double amount, final Account account,
                                            final User user) {
        BusinessAccount businessAccount;
        double convertedAmount;
        double exchangeRate = ExchangeOperations.getExchangeRate(exchangeRates,
                fromCurrency, toCurrency);
        convertedAmount = amount * exchangeRate;
        double commission = checkForCommission(account, convertedAmount, user.getPlan(),
                exchangeRates);
        if (account.getBalance() - (convertedAmount + commission) < 0) {
            Transactions transaction = new TransactionAction(command.getTimestamp(),
                    "Insufficient funds");
            addTransactionToObservers(transaction, user, account);
            return true;
        }
        if (account.getAccountType().equals("business")
                && !account.getEmail().equals(command.getEmail())) {
            businessAccount = (BusinessAccount) account;
            BusinessUser businessUser = null;
            if (businessAccount.getManagers().containsKey(command.getEmail())) {
                businessUser = businessAccount.getManagers().get(command.getEmail());
            } else if (businessAccount.getEmployees().containsKey(command.getEmail())) {
                businessUser = businessAccount.getEmployees().get(command.getEmail());
            }

            if (businessUser.getRole().equals("employee")) {
                if (businessAccount.getSpendingLimit() != 0
                        && convertedAmount > businessAccount.getSpendingLimit()) {
                    return true;
                }
            }
            businessUser.getSpent().put(command.getTimestamp(), convertedAmount);
            HashMap<Integer, String> spendings = businessUser.getToCommerciantsSpendingList();
            spendings.put(command.getTimestamp(), command.getCommerciant());
            if (!businessAccount.getCommerciants().contains(command.getCommerciant())) {
                businessAccount.getCommerciants().add(command.getCommerciant());
            }
        }
        account.pay(convertedAmount);
        CashbackOperations.getCashback(account,
                CommerciantOperation.findCommerciant(command.getCommerciant(),
                        commerciants), convertedAmount, user, exchangeRates);
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
        checkForGoldUpgrade(command, amountInRON, user, account);
        return false;
    }

    /**
     * Check for gold upgrade
     * @param command command input
     * @param amountInRON amount in RON
     * @param user user
     * @param account account
     */
    private static void checkForGoldUpgrade(final CommandInput command,
                                            final double amountInRON, final User user,
                                            final Account account) {
        if (amountInRON >= LIMIT_FOR_UPGRADE && user.getPlan().equals("silver")) {
            user.setNrOfPaymentsForUpgrade(user.getNrOfPaymentsForUpgrade() + 1);
            if (user.getNrOfPaymentsForUpgrade() == MAXIMUM_PAYMENTS_FOR_UPGRADE) {
                user.setPlan("gold");
                Transactions transactionUpgrade = new TransactionUpdatePlan(account.getIban(),
                        "gold", "Upgrade plan", command.getTimestamp());
                addTransactionToObservers(transactionUpgrade, user, account);
            }
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
            erroruserNotFoundJson(command, output, mapper);
            return;
        }

        Account toAccount = null;
        Account fromAccount;
        if (!aliasAccountMap.containsKey(from)) {
            if (!accountMap.containsKey(from)) {
                erroruserNotFoundJson(command, output, mapper);
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
                    erroruserNotFoundJson(command, output, mapper);
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
            erroruserNotFoundJson(command, output, mapper);
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

        if (fromAccount.getAccountType().equals("business")) {
            businessAccount = (BusinessAccount) fromAccount;
        }
        if (fromAccount.getAccountType().equals("business")
                && !fromAccount.getEmail().equals(command.getEmail())) {
            BusinessUser businessUser = null;
            if (businessAccount.getManagers().containsKey(command.getEmail())) {
                businessUser = businessAccount.getManagers().get(command.getEmail());
            } else if (businessAccount.getEmployees().containsKey(command.getEmail())) {
                businessUser = businessAccount.getEmployees().get(command.getEmail());
            }
            if (businessUser.getRole().equals("employee")) {
                if (businessAccount.getSpendingLimit() != 0
                        && amount > businessAccount.getSpendingLimit()) {
                    return;
                }
            }
            businessUser.getSpent().put(command.getTimestamp(), amount);
        }
        fromAccount.pay(amount);
        fromAccount.pay(commissionSender);
        if (commerciant != null) {
            if (businessAccount != null && !fromAccount.getEmail().equals(command.getEmail())) {
                BusinessUser businessUser = null;
                if (businessAccount.getManagers().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getManagers().get(command.getEmail());
                } else if (businessAccount.getEmployees().containsKey(command.getEmail())) {
                    businessUser = businessAccount.getEmployees().get(command.getEmail());
                }
                HashMap<Integer, String> spendings = businessUser.getToCommerciantsSpendingList();
                spendings.put(command.getTimestamp(), commerciant.getName());
                if (!businessAccount.getCommerciants().contains(commerciant.getName())) {
                    businessAccount.getCommerciants().add(commerciant.getName());
                }
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
     * Error user not found json
     * @param command command input
     * @param output output array
     * @param mapper object mapper
     */
    private static void erroruserNotFoundJson(final CommandInput command,
                                              final ArrayNode output, final ObjectMapper mapper) {
        ObjectNode error = mapper.createObjectNode();
        error.put("command", command.getCommand());
        ObjectNode outNode = mapper.createObjectNode();
        outNode.put("timestamp", command.getTimestamp());
        outNode.put("description", "User not found");
        error.set("output", outNode);
        error.put("timestamp", command.getTimestamp());
        output.add(error);
    }
}
