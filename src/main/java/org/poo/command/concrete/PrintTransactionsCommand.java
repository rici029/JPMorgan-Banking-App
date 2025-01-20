package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.Transactions;
import org.poo.user.User;

public class PrintTransactionsCommand extends BaseCommand {
    public PrintTransactionsCommand(final CommandInput command, final AppContext context) {
        super(command, context);
    }

    /**
     * Executes the command.
     */
    @Override
    public void execute() {
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
}
