package org.poo.command.concrete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.appOperations.PrintOperations;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.Transactions;
import org.poo.user.User;

public class DeleteAccountCommand extends BaseCommand {
    public DeleteAccountCommand(final CommandInput command, final AppContext context) {
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
//        AccountOperations.deleteAccount(usersAccountsMap, command, output);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("command", command.getCommand());
        String iban = command.getAccount();
        if (!usersAccountsMap.containsKey(iban)) {
            return;
        }
        User user = usersAccountsMap.get(iban);
        for (Account account : user.getAccounts()) {
            if (account.getIban().equals(iban)) {
                if (account.getBalance() == 0) {
                    user.removeAccount(account);
                    usersAccountsMap.remove(iban);
                    output.add(PrintOperations.printSuccessJson(command));
                    break;
                } else {
                    ObjectNode error = mapper.createObjectNode();
                    error.put("error",
                            "Account couldn't be deleted - see org.poo.transactions for details");
                    error.put("timestamp", command.getTimestamp());
                    response.set("output", error);
                    response.put("timestamp", command.getTimestamp());
                    output.add(response);
                    Transactions transaction = new TransactionAction(command.getTimestamp(),
                            "Account couldn't be deleted - there are funds remaining");
                    addTransactionToObservers(transaction, user, account);
                }
            }
        }
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
}
