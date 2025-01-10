package org.poo.appOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionPayment;
import org.poo.transactions.Transactions;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public final class PrintOperations {

    private PrintOperations() {
        //not called
    }

    /**
     * Print the users in the output
     * @param output the output
     * @param users the users
     * @param command the command
     */
    public static void printUsers(final ArrayNode output, final ArrayList<User> users,
                                  final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ArrayNode usersArray = mapper.createArrayNode();
        for (User user : users) {
            ObjectNode userNode = user.printJson(mapper);
            ArrayNode accountsArray = mapper.createArrayNode();
            for (Account account : user.getAccounts()) {
                ObjectNode accountNode = account.printJson(mapper);
                ArrayNode cardsArray = mapper.createArrayNode();
                for (Card card : account.getCards()) {
                    ObjectNode cardNode = mapper.createObjectNode();
                    cardNode.put("cardNumber", card.getCardNumber());
                    cardNode.put("status", card.getCardStatus());
                    cardsArray.add(cardNode);
                }
                accountNode.set("cards", cardsArray);
                accountsArray.add(accountNode);
            }
            userNode.set("accounts", accountsArray);
            usersArray.add(userNode);
        }

        objectNode.set("output", usersArray);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);
    }

    /**
     *
     * @param command the command
     * @return the account
     */
    public static ObjectNode printSuccessJson(final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode outNode = mapper.createObjectNode();
        outNode.put("command", command.getCommand());
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("success", "Account deleted");
        successNode.put("timestamp", command.getTimestamp());
        outNode.set("output", successNode);
        outNode.put("timestamp", command.getTimestamp());
        return outNode;
    }

    /**
     *
     * @param output the output
     * @param command the command
     */
    public static void cardNotFound(final ArrayNode output, final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("timestamp", command.getTimestamp());
        errorNode.put("description", "Card not found");
        objectNode.set("output", errorNode);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);
    }

    /**
     *
     * @param output the output
     * @param users the users
     * @param command the command
     */
    public static void printTransactions(final ArrayNode output, final ArrayList<User> users,
                                         final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ArrayNode outputArray = mapper.createArrayNode();
        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                for (Transactions transaction : user.getTransactions()) {
                    ObjectNode transactionNode = transaction.printJson(mapper);
                    outputArray.add(transactionNode);
                }
                break;
            }
        }
        objectNode.set("output", outputArray);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);
    }

    /**
     *
     * @param command  the command
     * @param accountMap the account map
     * @param output the output
     */
    public static void report(final CommandInput command,
                              final HashMap<String, Account> accountMap, final ArrayNode output) {
        String iban = command.getAccount();
        if (!accountMap.containsKey(iban)) {
            accountNotFound(output, command);
            return;
        }
        Account account = accountMap.get(iban);
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode outNode = mapper.createObjectNode();
        outNode.put("command", command.getCommand());
        ObjectNode accountNode = mapper.createObjectNode();
        accountNode.put("IBAN", iban);
        accountNode.put("balance", account.getBalance());
        accountNode.put("currency", account.getCurrency());
        ArrayNode transactionsArray = mapper.createArrayNode();
        for (Transactions transaction : account.getTransactions()) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp) {
                ObjectNode objectNode = transaction.printJson(mapper);
                transactionsArray.add(objectNode);
            }
        }
        accountNode.set("transactions", transactionsArray);
        outNode.set("output", accountNode);
        outNode.put("timestamp", command.getTimestamp());
        output.add(outNode);
    }

    /**
     *
     * @param output the output
     * @param command the command
     */
    public static void accountNotFound(final ArrayNode output, final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("timestamp", command.getTimestamp());
        errorNode.put("description", "Account not found");
        objectNode.set("output", errorNode);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);
    }

    /**
     *
     * @param command the command
     * @param accountMap the account map
     * @param output the output
     */
    public static void spendingsReport(final CommandInput command,
                                       final HashMap<String, Account> accountMap,
                                       final ArrayNode output) {
        TreeMap<String, Double> spendings = new TreeMap<>();

        if (!accountMap.containsKey(command.getAccount())) {
            accountNotFound(output, command);
            return;
        }
        Account account = accountMap.get(command.getAccount());

        if (account.getAccountType().equals("savings")) {
            accountError(output, command);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode outNode = mapper.createObjectNode();

        ObjectNode accountNode = printJsonAccountInfo(account, mapper);

        outNode.put("command", command.getCommand());

        ArrayNode transactionsArray = mapper.createArrayNode();

        for (Transactions transaction : account.getTransactions()) {
            if (transaction.getDescription().equals("Card payment")) {
                if (transaction.getTimestamp() >= command.getStartTimestamp()
                        && transaction.getTimestamp() <= command.getEndTimestamp()) {
                    TransactionPayment payment = (TransactionPayment) transaction;
                    if (spendings.containsKey(payment.getCommerciant())) {
                        double amount = spendings.get(payment.getCommerciant());
                        amount += payment.getAmount();
                        spendings.put(payment.getCommerciant(), amount);
                    } else {
                        spendings.put(payment.getCommerciant(), payment.getAmount());
                    }
                    ObjectNode transactionNode = payment.printJson(mapper);
                    transactionsArray.add(transactionNode);
                }
            }
        }

        ArrayNode commerciantsArray = mapper.createArrayNode();

        for (String commerciant : spendings.keySet()) {
            ObjectNode spendingNode = mapper.createObjectNode();
            spendingNode.put("commerciant", commerciant);
            spendingNode.put("total", spendings.get(commerciant));
            commerciantsArray.add(spendingNode);
        }

        accountNode.set("transactions", transactionsArray);
        accountNode.set("commerciants", commerciantsArray);
        outNode.set("output", accountNode);
        outNode.put("timestamp", command.getTimestamp());
        output.add(outNode);
    }

    /**
     *
     * @param account the account
     * @param mapper the mapper
     * @return the account node
     */
    public static ObjectNode printJsonAccountInfo(final Account account,
                                                  final ObjectMapper mapper) {
        ObjectNode accountNode = mapper.createObjectNode();
        accountNode.put("IBAN", account.getIban());
        accountNode.put("balance", account.getBalance());
        accountNode.put("currency", account.getCurrency());
        return accountNode;
    }

    /**
     *
     * @param output the output
     * @param command the command
     */
    private static void accountError(final ArrayNode output, final CommandInput command) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", command.getCommand());
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("error", "This kind of report is not supported for a saving account");
        objectNode.set("output", errorNode);
        objectNode.put("timestamp", command.getTimestamp());
        output.add(objectNode);
    }
}
