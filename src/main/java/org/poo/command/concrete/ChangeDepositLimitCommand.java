package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.HashMap;

@Getter @Setter
public class ChangeDepositLimitCommand extends BaseCommand {
    private HashMap<String, User> usersMap;
    public ChangeDepositLimitCommand(final CommandInput command,
                                     final AppContext context,
                                     final HashMap<String, User> usersMap) {
        super(command, context);
        this.usersMap = usersMap;
    }

    /**
     * Method that executes the change deposit limit command.
     */
    public void execute() {
        String userEmail = command.getEmail();
        String accountIban = command.getAccount();
        if (!usersMap.containsKey(userEmail)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "changeDepositLimit");
            ObjectNode out = mapper.createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", command.getTimestamp());
            error.set("output", out);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }
        Account account = accountMap.get(accountIban);
        if (!account.getAccountType().equals("business")) {
            return;
        }
        BusinessAccount businessAccount = (BusinessAccount) account;
        if (businessAccount.getEmail().equals(userEmail)) {
            businessAccount.setDepositLimit((command.getAmount()));
        } else {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "changeDepositLimit");
            ObjectNode out = mapper.createObjectNode();
            out.put("description", "You must be owner in order to change deposit limit.");
            out.put("timestamp", command.getTimestamp());
            error.set("output", out);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
        }
    }
}
