package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.account.AccountSavings;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionInterestRate;
import org.poo.transactions.Transactions;
import org.poo.user.User;

public class AddInterestCommand extends BaseCommand {
    public AddInterestCommand(final CommandInput command, final AppContext context) {
        super(command, context.getOutput(), context.getExchangeRates(),
              context.getUsers(), context.getUsersAccountsMap(),
              context.getUsersCardsMap(), context.getCardAccountMap(),
              context.getAccountMap(), context.getAliasAccountMap());
    }

    /**
     * Execute the command.
     */
    @Override
    public void execute() {
//        AccountOperations.addInterest(accountMap, command, output, usersAccountsMap);
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
        User user = usersAccountsMap.get(iban);
        transaction.registerObserver(user);
        transaction.registerObserver(account);
        transaction.notifyObservers();
    }
}
