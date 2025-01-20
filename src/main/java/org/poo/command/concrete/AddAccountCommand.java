
package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.account.AccountFactory;
import org.poo.command.BaseCommand;
import org.poo.command.AppContext;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.Transactions;
import org.poo.user.User;


public class AddAccountCommand extends BaseCommand {
    public AddAccountCommand(final CommandInput command, final AppContext context) {
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
        String email = command.getEmail();
        String currency = command.getCurrency();
        String accountType = command.getAccountType();
        double interestRate = command.getInterestRate();
        Account account = AccountFactory.createAccount(email, currency, accountType,
                interestRate, exchangeRates);
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                user.addAccount(account);
                usersAccountsMap.put(account.getIban(), user);
                accountMap.put(account.getIban(), account);
                Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "New account created");
                addTransactionToObservers(transaction, user, account);
                break;
            }
        }
    }

    /**
     * Add a transaction to the observers
     * @param transaction transaction
     * @param user user
     * @param account account
     */
    private static void addTransactionToObservers(final Transactions transaction,
                                                 final User user, final Account account) {
        transaction.registerObserver(user);
        transaction.registerObserver(account);
        transaction.notifyObservers();
    }
}
