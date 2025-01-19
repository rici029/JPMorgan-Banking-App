package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.splitPayment.SplitPayment;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.HashMap;

public class RejectSplitPaymentCommand extends BaseCommand {
    private ArrayList<SplitPayment> splitPayments;
    private HashMap<String, User> usersMap;

    public RejectSplitPaymentCommand(final CommandInput command, final AppContext context,
                                     final ArrayList<SplitPayment> SplitPayments,
                                     final HashMap<String, User> usersMap) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
        this.splitPayments = SplitPayments;
        this.usersMap = usersMap;
    }

    public void execute() {
        String userEmail = command.getEmail();
        String type = command.getSplitPaymentType();
        if (!usersMap.containsKey(userEmail)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "rejectSplitPayment");
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
        for(SplitPayment splitPayment : splitPayments) {
            for(Account account : accounts) {
                if(splitPayment.getAccounts().contains(account.getIban())
                        && splitPayment.getType().equals(type)) {
                    splitPaymentNeeded = splitPayment;
                    break;
                }
            }
        }
        if(splitPaymentNeeded == null)
            return;
        splitPayments.remove(splitPaymentNeeded);
        splitPaymentNeeded.rejected(accountMap, usersAccountsMap);
    }
}
