package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.appOperations.PrintOperations;
import org.poo.card.Card;
import org.poo.command.AppContext;
import org.poo.command.BaseCommand;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionAction;
import org.poo.transactions.Transactions;
import org.poo.user.User;

public class CheckCardStatusCommand extends BaseCommand {
    public CheckCardStatusCommand(final CommandInput command, final AppContext context) {
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
        String cardNumber = command.getCardNumber();
        if (!usersCardsMap.containsKey(cardNumber)) {
            PrintOperations.cardNotFound(output, command);
            return;
        }

        User user = usersCardsMap.get(cardNumber);
        Account account = cardAccountMap.get(cardNumber);
        for (Card card : account.getCards()) {
            if (card.getCardNumber().equals(cardNumber)) {
                if (account.getBalance() <= account.getMinimumBalance()) {
                    Transactions transaction = new TransactionAction(command.getTimestamp(),
                        "You have reached the minimum amount of funds, the card will be frozen");
                    addTransactionToObservers(transaction, user, account);
                    card.setCardStatus("frozen");
                }
                return;
            }
        }
        PrintOperations.cardNotFound(output, command);
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
