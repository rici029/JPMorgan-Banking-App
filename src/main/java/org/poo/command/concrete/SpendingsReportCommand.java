package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionPayment;
import org.poo.transactions.Transactions;

import java.util.TreeMap;

public class SpendingsReportCommand extends BaseCommand {
    public SpendingsReportCommand(final CommandInput command, final AppContext context) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
//        PrintOperations.spendingsReport(command, accountMap, output);
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
    private static ObjectNode printJsonAccountInfo(final Account account,
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
    private static void accountNotFound(final ArrayNode output, final CommandInput command) {
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
