package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.Transactions;

public class ReportCommand extends BaseCommand {
    public ReportCommand(final CommandInput command, final AppContext context) {
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
}
