package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.BusinessAccount;
import org.poo.businessUser.BusinessUser;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.HashMap;

@Getter @Setter
public class AddNewBusinessAssociateCommand extends BaseCommand {
    private HashMap<String, User> usersMap;

    public AddNewBusinessAssociateCommand(CommandInput commandInput, AppContext context, HashMap<String, User> usersMap) {
        super(commandInput, context.getOutput(), context.getExchangeRates(),
                context.getUsers(), context.getUsersAccountsMap(),
                context.getUsersCardsMap(), context.getCardAccountMap(),
                context.getAccountMap(), context.getAliasAccountMap());
        this.usersMap = usersMap;
    }

    public void execute() {
        String userEmail = command.getEmail();
        String accountIban = command.getAccount();
        if (!usersMap.containsKey(userEmail)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "addNewBusinessAssociate");
            ObjectNode out = mapper.createObjectNode();
            out.put("description", "User not found");
            out.put("timestamp", command.getTimestamp());
            error.set("output", out);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }
        User user = usersMap.get(userEmail);
        String role = command.getRole();
        BusinessAccount businessAccount = (BusinessAccount) accountMap.get(accountIban);
        BusinessUser businessUser = new BusinessUser(user.getFirstName() + " " + user.getLastName(),
                user.getEmail(), role);
        HashMap<String, BusinessUser> managers = businessAccount.getManagers();
        HashMap<String, BusinessUser> employees = businessAccount.getEmployees();
        if(managers.containsKey(userEmail) || employees.containsKey(userEmail)) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode error = mapper.createObjectNode();
            error.put("command", "addNewBusinessAssociate");
            ObjectNode out = mapper.createObjectNode();
            out.put("description", "The user is already an associate of the account.");
            out.put("timestamp", command.getTimestamp());
            error.set("output", out);
            error.put("timestamp", command.getTimestamp());
            output.add(error);
            return;
        }
        if (role.equals("manager")) {
            managers.put(userEmail, businessUser);
        } else {
            employees.put(userEmail, businessUser);
        }
    }
}
