package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.splitPayment.SplitPayment;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;

@Getter @Setter
public class AcceptSplitPaymentCommand extends BaseCommand {
    private ArrayList<SplitPayment> splitPayments;
    private HashMap<String, User> usersMap;
    public AcceptSplitPaymentCommand(final CommandInput command, final AppContext context,
                                     final ArrayList<SplitPayment> splitPayments,
                                     final HashMap<String, User> usersMap) {
        super(command, context);
        this.splitPayments = splitPayments;
        this.usersMap = usersMap;
    }

    /**
     * Method that executes the acceptSplitPayment command.
     */
    public void execute() {
        String userEmail = command.getEmail();
        String type = command.getSplitPaymentType();
        if (!usersMap.containsKey(userEmail)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "acceptSplitPayment");
            ObjectNode out = mapper.createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", command.getTimestamp());
            error.set("output", out);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }
        ArrayList<Account> accounts = usersMap.get(userEmail).getAccounts();
        SplitPayment splitPaymentNeeded = null;
        loop:
        for (SplitPayment splitPayment : splitPayments) {
            for (Account account : accounts) {
                if (splitPayment.getAccounts().contains(account.getIban())
                        && splitPayment.getType().equals(type)) {
                    splitPayment.setNoOfAccepts(splitPayment.getNoOfAccepts() + 1);
                    splitPaymentNeeded = splitPayment;
                    break loop;
                }
            }
        }
        if (splitPaymentNeeded == null) {
            return;
        }

        if (splitPaymentNeeded.getNoOfAccepts() == splitPaymentNeeded.getAccounts().size()) {
            splitPaymentNeeded.startPayment(exchangeRates, accountMap, usersAccountsMap);
            splitPayments.remove(splitPaymentNeeded);
        }
    }
}
