
package org.poo.command.concrete;

import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.command.BaseCommand;
import org.poo.command.AppContext;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionCard;
import org.poo.transactions.Transactions;
import org.poo.user.User;

import java.util.HashMap;

public class CreateCardCommand extends BaseCommand {
    private final HashMap<String, User> usersMap;
    public CreateCardCommand(final CommandInput command, final AppContext context,
                             final HashMap<String, User> usersMap) {
        super(command, context);
        this.usersMap = usersMap;
    }

    /**
     * Execute the command
     */
    @Override
    public void execute() {
        String email = command.getEmail();
        String cardType;
        if (command.getCommand().equals("createCard")) {
            cardType = "classic";
        } else {
            cardType = "onetime";
        }
        String iban = command.getAccount();
        User user = usersMap.get(email);
        Account account = accountMap.get(iban);
        if (user == null) {
            return;
        }
        Card card = new Card(iban, email, cardType);
        account.addCard(card);
        usersCardsMap.put(card.getCardNumber(), user);
        cardAccountMap.put(card.getCardNumber(), account);
        Transactions transaction = new TransactionCard(card.getCardNumber(),
                account.getIban(), user.getEmail(), "New card created",
                command.getTimestamp());
        addTransactionToObservers(transaction, user, account);
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
